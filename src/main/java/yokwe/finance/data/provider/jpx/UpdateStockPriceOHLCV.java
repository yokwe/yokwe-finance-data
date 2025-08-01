package yokwe.finance.data.provider.jpx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.Makefile;
import yokwe.finance.data.provider.UpdateBase;
import yokwe.finance.data.type.OHLCV;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.util.UnexpectedException;
import yokwe.util.json.JSON;

public class UpdateStockPriceOHLCV  extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageJPX.StockCodeName, StorageJPX.StockDetailJSON).
		output(StorageJPX.StockPriceOHLCV).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var stockList = StorageJPX.StockCodeName.getList();
		
		delistUnknownFile(stockList);
		
		int count = 0;
		for(var stock: stockList) {
			if ((++count % 1000) == 1) logger.info("{}  /  {}", count, stockList.size());
			
			var string = StorageJPX.StockDetailJSON.load(stock.stockCode);
			var result = JSON.unmarshal(StockDetail.class, string);
			updatePrice(stock, result);
		}
		
		// touch file
		StorageJPX.StockPriceOHLCV.touch();
	}
	
	protected void delistUnknownFile(List<StockCodeName> stockList) {
		var validNameList = stockList.stream().map(o -> o.stockCode).toList();
		StorageJPX.StockPriceOHLCV.delistUnknownFile(validNameList);
	}
	private void updatePrice(StockCodeName stock, StockDetail result) {
		if (result.section1.data == null) {
			logger.warn("data is null  {}  {}", stock.stockCode, stock.name);
			logger.warn("  result  {}", result.toString());
			return;
		}
		
		for(var data: result.section1.data.values()) {
			var stockCode = StockCodeJP.toStockCode5(data.TTCODE2);
			
			var oldList   = StorageJPX.StockPriceOHLCV.getList(stockCode);
			var oldMap    = oldList.stream().collect(Collectors.toMap(o -> o.date, Function.identity()));

			var newList   = getPriceList(data);
			
			// build oldList
			for(var newValue: newList) {
				var date = newValue.date;
				if (oldMap.containsKey(date)) {
					var oldValue = oldMap.get(date);
					if (oldValue.equals(newValue)) {
						// expected
					} else {
						// stock split
						//   return newList
						logger.info("stock split  {}  {}  {}", stockCode, oldValue, newValue);
						StorageJPX.StockPriceOHLCV.save(stockCode, newList);
						return;
					}
				} else {
					// add new entry to oldList
					oldList.add(newValue);
				}
			}
			StorageJPX.StockPriceOHLCV.save(stockCode, oldList);
		}
	}	
	private List<OHLCV> getPriceList(StockDetail.Data data) {
		List<OHLCV> priceList = new ArrayList<>();
		if (data.A_HISTDAYL.isEmpty()) return priceList;
		{
			var stockCode = StockCodeJP.toStockCode5(data.TTCODE2);
//			logger.info("stock  {}  {}", stockCode, data.FLLN);

			BigDecimal o = null;
			BigDecimal h = null;
			BigDecimal l = null;
			BigDecimal c = null;
			
			for(var ohlcvString: data.A_HISTDAYL.split(",?\\\\n")) {
				String[] valueString = ohlcvString.split(",");
				if (valueString.length != 6 && valueString.length != 7) {
					logger.error("Unexpected ohlcvString");
					logger.error("  stockCode    {}", stockCode);
					logger.error("  valueString  {}", valueString.length);
					logger.error("  ohlcvString  {}", ohlcvString);
					throw new UnexpectedException("Unexpected ohlcvString");
				}
				
				var dateString = valueString[0];
				var oString    = valueString[1];
				var hString    = valueString[2];
				var lString    = valueString[3];
				var cString    = valueString[4];
				var vString    = valueString[5];
				
				var v = Long.parseLong(vString);
				if (v == 0) {
					if (o == null) continue;
					// use last value
				} else {
					o = new BigDecimal(oString);
					h = new BigDecimal(hString);
					l = new BigDecimal(lString);
					c = new BigDecimal(cString);
				}
				
				var date = LocalDate.parse(dateString.replace('/', '-'));
				
				priceList.add(new OHLCV(date, o, h, l, c, v));
			}
			
			// FIXME add latest price
		}
		Collections.sort(priceList);
		return priceList;
	}
}
