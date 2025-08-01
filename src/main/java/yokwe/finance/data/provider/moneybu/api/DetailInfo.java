package yokwe.finance.data.provider.moneybu.api;

import java.math.BigDecimal;

import yokwe.finance.data.type.StockCodeJP;
import yokwe.util.ToString;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

// https://jpx.cloud.qri.jp/tosyo-moneybu/api/detail/info
public final class DetailInfo {
    public static final class Data {
        @JSON.Ignore public int               categoryCode;
                     public String            categoryName;
                     public String            date;         // YYYY/MM/DD
        @JSON.Ignore public BigDecimal        depth;
        @JSON.Ignore public String            depthDate;
        
        @JSON.Ignore public BigDecimal        deviation;     // 乖離率…前日市場価格÷前日基準価額を1口あたりに換算した値の割合を表示
        @JSON.Ignore public String[]          disclaimer;
        @JSON.Ignore public BigDecimal        dividend;      // 分配金…直近1年間の分配金額を表示
                     public String            dividendDate;  // （年4回）
                     public DividendHist[]    dividendHist;
        @JSON.Ignore public BigDecimal        dividendYield; // 分配金利回り
        @JSON.Ignore public int               exType;        // 0 for ETF
        @JSON.Ignore public int               favoriteCount;
        @JSON.Ignore public String            feature;       // explanation
        @JSON.Ignore public boolean           hasNav;
        @JSON.Ignore public String            iNavDate;
        
        @JSON.Ignore public Icons             icons;
        
        @JSON.Ignore public int               inav;
        @JSON.Ignore public int               isFavorite;
        @JSON.Ignore public BigDecimal        liquidity;
                     public String            listingDate;       // YYYY/MM/DD
        @JSON.Ignore public ManagementCompany managementCompany;
        			 public BigDecimal        managementFee;     // 信託報酬 in percent
        @JSON.Ignore public BigDecimal        managementFeeTotal;
        @JSON.Ignore public int               marketMake;
        @JSON.Ignore public int               minInvest;
                     public String            nav;               // 基準価額 "2,042.94"
        @JSON.Ignore public long              netAssets;         // 純資産総額
        @JSON.Ignore public String            netAssetsDate;     // YYYY/MM/DD
        @JSON.Ignore public String            notice;
        @JSON.Ignore public String            nriDate;
        @JSON.Ignore public String            otherExpense;
        
        @JSON.Ignore public String            pcfDataDate;
        @JSON.Ignore public String            pcfFundDate;
        
        @JSON.Ignore public PcfWeight         pcfWeight;

        @JSON.Ignore public BigDecimal        price;     // 終値・直近取引値
        @JSON.Ignore public String            priceDate; // YYYY/MM/DD
        
        @JSON.Ignore public int               productCode;
        @JSON.Ignore public String            productType;
        
        @JSON.Ignore public long              quarterTradingValue;  // 平均売買代金（直近90日）
        @JSON.Ignore public long              quarterTradingVolume; // 平均売買高（直近90日）
        
        @JSON.Ignore public int               reserve;
        
        @JSON.Ignore public long              rightUnit;  // 受益権口数
        @JSON.Ignore public String            sharesDate; // YYYY/MM/DD
        
                     public BigDecimal        shintakuRyuhogaku;
        
        @JSON.Ignore public BigDecimal        spread;     // スプレッド…最良の売気配値段と買気配値段の価格差（%）
        @JSON.Ignore public String            spreadDate; // STRING STRING
        
                     public String            stockCode; // NNNN
                     public String            stockName;
        
        @JSON.Ignore public String            targetIndex;      // 対象指標
        @JSON.Ignore public String            underlierOutline; // explanation of underline index
        
        @JSON.Ignore public BigDecimal        tradingValue; // 売買代金
        @JSON.Ignore public String            tvDate;       // YYYY/MM/DD
        
        @JSON.Ignore public int               unit;   // 売買単位
        @JSON.Ignore public long              volume; // 売買高

        @JSON.Ignore public String            yokogaoLink;
        
        @Override
        public String toString() {
            return ToString.withFieldName(this);
        }
    }

    @JSON.Optional public Data   data;
                   public String status;
    @JSON.Optional public String message;

    @Override
    public String toString() {
        return ToString.withFieldName(this);
    }
    
    public static String download(String stockCode) {
    	var body        = String.format("{\"stockCode\": \"%s\"}", StockCodeJP.toStockCode4(stockCode));
		var contentType = "application/json;charset=UTF-8";
		var url         = "https://jpx.cloud.qri.jp/tosyo-moneybu/api/detail/info";
		var string      = HttpUtil.getInstance().withPost(body, contentType).downloadString(url);
		
		return string;
    }
}