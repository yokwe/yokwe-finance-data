package yokwe.finance.data.provider.moneybu;

import yokwe.util.CSVUtil;

public class ETF implements Comparable<ETF> {
	@CSVUtil.ColumnName("コード")															public String stockCode;
	@CSVUtil.ColumnName("銘柄名")															public String name;
	@CSVUtil.ColumnName("種類")																public String category;
	@CSVUtil.ColumnName("指数名/ベンチマーク")												public String indexName;
	@CSVUtil.ColumnName("管理会社/発行会社")												public String managementCompany;
	@CSVUtil.ColumnName("新NISA対象")														public String nisaFlag;        // 〇
	@CSVUtil.ColumnName("MM対象")															public String marketMakeFlag;  // 〇
	@CSVUtil.ColumnName("売買単位")															public String tradeUnits;
	@CSVUtil.ColumnName("上場日")															public String listingDate;     // 20010713
	@CSVUtil.ColumnName("ETF/ETN別")														public String flagETFETN;      // ETF or ETN
	@CSVUtil.ColumnName("最低買付金額日付")													public String lowestPriceDate; // 20010713
	@CSVUtil.ColumnName("最低買付金額(円)")													public String lowestPrice;
	@CSVUtil.ColumnName("純資産総額/残存償還価額総額(億円)")								public String nav;
	@CSVUtil.ColumnName("信託報酬等（％）")													public String expenceRatio;
	@CSVUtil.ColumnName("総経費率（％）")													public String managementFee;
	@CSVUtil.ColumnName("分配金利回り日付")													public String yieldDate;		// 20240930
	@CSVUtil.ColumnName("分配金利回り（分配金利回り日付が空欄の銘柄は表示されません。）")	public String yield;
	@CSVUtil.ColumnName("概要留意事項")														public String note;
	
	public static final String YES = "〇";

	@Override
	public int compareTo(ETF that) {
		return this.stockCode.compareTo(that.stockCode);
	}
}
