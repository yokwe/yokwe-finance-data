package yokwe.finance.data.stock.us;

import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.Makefile;
import yokwe.finance.data.provider.UpdateBase;
import yokwe.finance.data.provider.rakuten.StorageRakuten;
import yokwe.finance.data.provider.yahoo.StorageYahoo;
import yokwe.finance.data.type.StockInfoUS;

public class UpdateStockInfo extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageRakuten.TradingStockUS, StorageUS.StockInfoAll, StorageYahoo.CompanyInfoUS).
		output(StorageUS.StockInfo).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var rakutenSet = StorageRakuten.TradingStockUS.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		logger.info("rakuten  {}", rakutenSet.size());
		
		var tradingSet = new TreeSet<String>();
		tradingSet.addAll(rakutenSet);
		logger.info("trading  {}", tradingSet.size());
		
		var list = StorageUS.StockInfoAll.getList();
		logger.info("list     {}  all", list.size());
		
		list.removeIf(o -> !tradingSet.contains(o.stockCode));
		logger.info("list     {}  remove not in tradingSet", list.size());
		
		// supply sector and industry
		var companyInfoMap = StorageYahoo.CompanyInfoUS.getList().stream().collect(Collectors.toMap(o -> o.stockCode, Function.identity()));
		for(var stockInfo: list) {
			var stockCode = stockInfo.stockCode;
			if (stockInfo.type == StockInfoUS.Type.PREF) {
				stockInfo.sector    = "*" + stockInfo.type + "*";
				stockInfo.industry  = "*" + stockInfo.type + "*";
			} else {
				var companyInfo = companyInfoMap.get(stockCode);
				if (companyInfo != null) {
					stockInfo.sector    = companyInfo.sector;
					stockInfo.industry  = companyInfo.industry;
				} else {
					stockInfo.sector    = "*" + stockInfo.type + "*";
					stockInfo.industry  = "*" + stockInfo.type + "*";
				}
			}
		}
		
		checkAndSave(list, StorageUS.StockInfo);
	}
}
