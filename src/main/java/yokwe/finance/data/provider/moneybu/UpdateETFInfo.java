package yokwe.finance.data.provider.moneybu;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import yokwe.finance.data.provider.Makefile;
import yokwe.finance.data.provider.UpdateBase;
import yokwe.finance.data.provider.jpx.StockCodeName;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.util.CSVUtil;
import yokwe.util.Storage;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateETFInfo extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageMoneybu.ETFInfo).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	public void update() {
		Storage.initialize();
		
		var url    = "https://jpx.cloud.qri.jp/tosyo-moneybu/api/top/output";
		var string = HttpUtil.getInstance().downloadString(url);
		
		var pos = string.indexOf("\n");
		if (pos == -1) {
			throw new UnexpectedException("Unexpected pos");
		}
		var cvsString = string.substring(pos + 1);
		var etfList = CSVUtil.read(ETF.class).file(new StringReader(cvsString));
		StorageMoneybu.ETF.save(etfList);
		
		var list = new ArrayList<ETFInfo>();
		logger.info("etfList  {}", etfList.size());
		for(var e: etfList) {
			list.add(toETFInfo(e));
		}
		
		checkDuplicateKey(list, o -> o.stockCode);
		checkAndSave(list, StorageMoneybu.ETFInfo);
	}
	private static ETFInfo toETFInfo(ETF etf) {
		var stockCode = StockCodeJP.toStockCode5(etf.stockCode);
		var type      = toType(etf.flagETFETN);
		var category  = toSimpleCategory(etf.category);
		var name      = StockCodeName.getName(stockCode, StringUtil.toFullWidth(etf.name));
		return new ETFInfo(stockCode, type, category, name);
	}
	private static ETFInfo.Type toType(String string) {
		var ret = typeMap.get(string);
		if (ret != null) return ret;
		logger.error("Unexpected string");
		logger.error("  string  {}", string);
		throw new UnexpectedException("Unexpected string");
	}
	//
	private static Map<String, ETFInfo.Type> typeMap = Map.ofEntries(
		Map.entry("ETF", ETFInfo.Type.ETF),
		Map.entry("ETN", ETFInfo.Type.ETN)
	);
	private static String toSimpleCategory(String string) {
		var ret = categoryMap.get(string);
		if (ret != null) return ret;
		logger.error("Unexpected string");
		logger.error("  string  {}", string);
		throw new UnexpectedException("Unexpected string");
	}
	private static Map<String, String> categoryMap = Map.ofEntries(
			Map.entry("アクティブ運用型ETF",       "アクティブ運用"),
			Map.entry("インバース商品",            "インバース"),
			Map.entry("エンハンスト型ETF",         "エンハンスト"),
			Map.entry("バランス型ETF",             "バランス"),
			Map.entry("ボラティリティETF",         "ボラティリティ"),
			Map.entry("レバレッジ商品",            "レバレッジ"),
			Map.entry("不動産ETF",                 "不動産"),
			Map.entry("不動産ETN",                 "不動産"),
			Map.entry("商品ETF",                   "商品"),
			Map.entry("商品（外国投資法人債）ETF", "商品"),
			Map.entry("国内債券ETF",               "国内債券"),
			Map.entry("国内株ETF",                 "国内株"),
			Map.entry("国内株ETN",                 "国内株"),
			Map.entry("外国債券ETF",               "外国債券"),
			Map.entry("外国株ETF",                 "外国株"),
			Map.entry("外国株ETN",                 "外国株")
	);
}
