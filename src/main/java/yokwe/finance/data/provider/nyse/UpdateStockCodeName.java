package yokwe.finance.data.provider.nyse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.finance.data.provider.Makefile;
import yokwe.finance.data.provider.UpdateList;
import yokwe.finance.data.type.StockCodeNameUS;
import yokwe.finance.data.type.StockInfoUS.Market;
import yokwe.finance.data.type.StockInfoUS.Type;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateStockCodeName extends UpdateList<Filter> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageNYSE.StockCodeName).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	protected List<Filter> downloadFile() {
		var stockList = downloadFilter(TYPE_STOCK);
		var etfList   = downloadFilter(TYPE_ETF);
		logger.info("stock  {}", stockList.size());
		logger.info("etf    {}", etfList.size());
		
		// supply default value for stock and etf
		for(var e: stockList) {
			e.normalizedTicker = e.normalizedTicker.replace("p", "-");
			
			if (e.exchangeId == null)          e.exchangeId          = "";
			if (e.instrumentType == null)      e.instrumentType      = "COMMON_STOCK";
			if (e.symbolTicker == null)        e.symbolTicker        = e.normalizedTicker;
			if (e.symbolEsignalTicker == null) e.symbolEsignalTicker = "";
			if (e.micCode == null)             e.micCode             = "XNYS";
		}
		for(var e: etfList) {
			if (e.exchangeId == null)          e.exchangeId          = "";
			if (e.instrumentType == null)      e.instrumentType      = "EXCHANGE_TRADED_FUND";
			if (e.symbolTicker == null)        e.symbolTicker        = e.normalizedTicker;
			if (e.symbolEsignalTicker == null) e.symbolEsignalTicker = "";
			if (e.micCode == null)             e.micCode             = "XNYS";
		}
		
		var map = new TreeMap<String, Filter>();
		for(var e: stockList) {
			// skip unit
//			if (e.instrumentName.toUpperCase().contains(" UNIT ")) continue;
			if (e.normalizedTicker.endsWith(".U")) continue;
			if (e.normalizedTicker.length() == 5 && e.normalizedTicker.charAt(4) == 'U') continue;
			
			if (map.containsKey(e.normalizedTicker)) {
				var old = map.get(e.normalizedTicker);
				if (old.instrumentName.length() < e.instrumentName.length()) {
					logger.info("XX  {}", e.normalizedTicker);
					map.put(e.normalizedTicker, e);
				}
			} else {
				map.put(e.normalizedTicker, e);
			}
		}
		for(var e: etfList) {
			if (map.containsKey(e.normalizedTicker)) {
				var old = map.get(e.normalizedTicker);
				if (old.instrumentName.length() < e.instrumentName.length()) {
					logger.info("YY  {}", e.normalizedTicker);
					map.put(e.normalizedTicker, e);
				}
			} else {
				map.put(e.normalizedTicker, e);
			}
		}
		
		var list = new ArrayList<Filter>(map.values());
		StorageNYSE.Filter.save(list);

		// sanity check
		checkDuplicateKey(list, o -> o.symbolTicker);
		
//		StorageNYSE.Filter.save(list);
		
		return list;
	}
	private static final String TYPE_STOCK = "EQUITY";
	private static final String TYPE_ETF   = "EXCHANGE_TRADED_FUND";
	
	private List<Filter> downloadFilter(String instrumentType) {
		var body = String.format(BODY_FORMAT, instrumentType);
		
		var string = HttpUtil.getInstance().withPost(body, CONTENT_TYPE).downloadString(URL);
		FileUtil.write().file("tmp/nyse-" + instrumentType, string);
		
		
		
		var list = JSON.getList(Filter.class, string);
		
		// remove "," from instrumentName
		for(var e: list) e.instrumentName = e.instrumentName.replace(",", "");
		
		return list;
	}
	private static final String URL          = "https://www.nyse.com/api/quotes/filter";
	private static final String BODY_FORMAT  = "{\"instrumentType\":\"%s\",\"pageNumber\":1,\"sortColumn\":\"NORMALIZED_TICKER\",\"sortOrder\":\"ASC\",\"maxResultsPerPage\":10000,\"filterToken\":\"\"}";
	private static final String CONTENT_TYPE = "application/json";
	
	
	@Override
	protected void updateFile(List<Filter> filterList) {
		var list = new ArrayList<StockCodeNameUS>();
		
		int count = 0;
		int countSkip = 0;
		
		for(var e: filterList) {
			count++;
			if (e.symbolTicker.startsWith("E:")) {
				countSkip++;
				continue;
			}
			if (e.micCode.equals(MIC_UNLISTED)) {
				// this stock is unlisted stock
				countSkip++;
				continue;
			}
			
			String symbol   = e.symbolTicker;
			Market market   = toMarket(e.micCode);
			Type   type     = toType(e.instrumentType);
			String name     = e.instrumentName.toUpperCase(); // use upper case
			
			if (market == Market.NYSE && (type.isETF() || type.isStock())) {
				list.add(new StockCodeNameUS(symbol, market, type, name));
			} else {
				countSkip++;
			}
		}
		
		logger.info("total  {}", count);
		logger.info("skip   {}", countSkip);
		
		checkAndSave(list, StorageNYSE.StockCodeName);
	}
	private static final String MIC_UNLISTED = "XXXX";

	
	private Market toMarket(String string) {
		var ret = marketMap.get(string);
		if (ret != null) return ret;
		logger.error("Unpexpected string");
		logger.error("  string  {}", string);
		throw new UnexpectedException("Unpexpected string");
	}
	// ISO 10381  MIC CODE
	//   https://www.iso20022.org/market-identifier-codes
	private static final Map<String, Market> marketMap = Map.ofEntries(
		Map.entry("ARCX", Market.NYSE),   // NYSE ARCA
		Map.entry("BATS", Market.BATS),   // CBOE BZX U.S. EQUITIES EXCHANGE
		Map.entry("XASE", Market.NYSE),   // NYSE MKT LLC
		Map.entry("XNCM", Market.NASDAQ), // NASDAQ CAPITAL MARKET
		Map.entry("XNGS", Market.NASDAQ), // NASDAQ/NGS (GLOBAL SELECT MARKET)
		Map.entry("XNMS", Market.NASDAQ), // NASDAQ/NMS (GLOBAL MARKET)
		Map.entry("XNYS", Market.NYSE),   // NEW YORK STOCK EXCHANGE, INC.
		Map.entry("IEXG", Market.IEXG)    // INVESTORS EXCHANGE
	);
	
	private Type toType(String string) {
		var ret = typeMap.get(string);
		if (ret != null) return ret;
		logger.error("Unpexpected string");
		logger.error("  string  {}", string);
		throw new UnexpectedException("Unpexpected string");
	}
	private static final Map<String, Type> typeMap = Map.ofEntries(
		Map.entry("CLOSED_END_FUND",              Type.CEF),
		Map.entry("COMMON_STOCK",                 Type.COMMON),
		Map.entry("DEPOSITORY_RECEIPT",           Type.ADR),
		Map.entry("EXCHANGE_TRADED_FUND",         Type.ETF),
		Map.entry("EXCHANGE_TRADED_NOTE",         Type.ETN),
		Map.entry("LIMITED_PARTNERSHIP",          Type.LP),
		Map.entry("PREFERRED_STOCK",              Type.PREF),
		Map.entry("REIT",                         Type.REIT),
		Map.entry("TRUST",                        Type.TRUST),
		Map.entry("UNIT",                         Type.UNIT),
		Map.entry("UNITS_OF_BENEFICIAL_INTEREST", Type.UBI)
	);
}
