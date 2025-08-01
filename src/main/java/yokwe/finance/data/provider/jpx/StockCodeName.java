package yokwe.finance.data.provider.jpx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jreit.StorageJREIT;
import yokwe.finance.data.provider.moneybu.StorageMoneybu;
import yokwe.finance.data.type.CodeName;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.finance.data.type.StockInfoJP.Type;
import yokwe.util.StringUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public final class StockCodeName implements Comparable<StockCodeName>{
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public String stockCode;
	public String isinCode;
	public Type   type;
	public String name;
	
	public StockCodeName(String stockCode, String isinCode, Type type, String name) {
		this.stockCode = stockCode;
		this.isinCode  = isinCode;
		this.type      = type;
		this.name      = name;
	}
	
	public boolean isPreferredStock() {
		return StockCodeJP.isPreferredStock(stockCode);
	}
	
	@Override
	public int compareTo(StockCodeName that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	private static final Map<String, StockCodeName> stockCodeMap = new HashMap<>();
	//                       stockCode
	private static final Map<String, StockCodeName> isinCodeMap = new HashMap<>();
	//                       isinCode
	static {
		reload();
	}
	
	public static void reload() {
		stockCodeMap.clear();
		for(var e: StorageJPX.StockCodeName.getList()) {
			stockCodeMap.put(e.stockCode, e);
			isinCodeMap.put(e.isinCode, e);
		}
	}
	
	
	//
	// getXXX by stockCode
	//
	public static StockCodeName fromStockCode(String stockCode) {
		var ret = stockCodeMap.get(stockCode);
		if (ret != null) {
			return ret;
		} else {
			logger.error("Unexpected stockCode");
			logger.error("  stockCode  {}", stockCode);
			throw new UnexpectedException("Unexpected stockCode");
		}
	}
	
	public static String getName(String stockCode) {
		return fromStockCode(stockCode).name;
	}
	public static String getName(String stockCode, String defaultName) {
		return stockCodeMap.containsKey(stockCode) ? stockCodeMap.get(stockCode).name : StringUtil.toFullWidth(defaultName);
	}
	public static String getISINCode(String stockCode) {
		return fromStockCode(stockCode).isinCode;
	}
	public static String getISINCode(String stockCode, String devaultValue) {
		return stockCodeMap.containsKey(stockCode) ? stockCodeMap.get(stockCode).isinCode : devaultValue;
	}
	public static Type getType(String stockCode) {
		return fromStockCode(stockCode).type;
	}
	
	//
	// getXXX by isinCode
	//
	public static StockCodeName fromISINCode(String isinCode) {
		var ret = isinCodeMap.get(isinCode);
		if (ret != null) {
			return ret;
		} else {
			logger.error("Unexpected isinCode");
			logger.error("  isinCode  {}", isinCode);
			throw new UnexpectedException("Unexpected isinCode");
		}
	}
	public static String getStockCode(String isinCode) {
		return fromISINCode(isinCode).stockCode;
	}
	public static String getStockCode(String stockCode, String devaultValue) {
		return isinCodeMap.containsKey(stockCode) ? isinCodeMap.get(stockCode).stockCode : devaultValue;
	}
	
	
	public static void checkStockCodeName() {
		// check and fix other
		var list = StorageJPX.StockCodeName.getList();
		checkMyList("ETF",       list, StorageJPX.ETF.getList().stream().map(o ->new CodeName(o.code, o.name)).toList());
		checkMyList("ETN",       list, StorageJPX.ETN.getList().stream().map(o ->new CodeName(o.code, o.name)).toList());
		checkMyList("Infra",     list, StorageJPX.Infra.getList().stream().map(o ->new CodeName(o.code, o.name)).toList());
		checkMyList("REIT",      list, StorageJPX.REIT.getList().stream().map(o ->new CodeName(o.code, o.name)).toList());
		checkMyList("JREIT",     list, StorageJREIT.JREITInfo.getList().stream().map(o ->new CodeName(o.stockCode, o.name)).toList());
		checkMyList("MoneyBu",   list, StorageMoneybu.ETFInfo.getList().stream().map(o ->new CodeName(o.stockCode, o.name)).toList());
	}
	private static void checkMyList(String group, List<StockCodeName> list, List<CodeName> myList) {
		int count = 0;
		logger.info("check  {}  {}", group, myList.size());
		var map = list.stream().collect(Collectors.toMap(o -> o.stockCode, o -> o.name));
		for(var e: myList) {
			var name = map.get(e.code);
			if (name == null) {
				if (count++ == 0) logger.info("====");
				// not found in map
				logger.warn("{}  missing  {}  {}", group, e.code, e.name);
			} else {
				// found in map
				if (name.equals(e.name)) {
					// expect
				} else {
					if (count++ == 0) logger.info("====");
					logger.warn("{}  fix      {}  {}  <-  {}", group, e.code, e.name, name);
					count++;
				}
			}
		}
		if (count != 0) logger.info("====");
	}
}
