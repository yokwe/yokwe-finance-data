package yokwe.finance.data.provider.moneybu.api;

import java.math.BigDecimal;
import java.util.UUID;

import yokwe.util.ToString;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

// https://jpx.cloud.qri.jp/tosyo-moneybu/api/list/etf
public final class ListETF {
	public static final class Data {
						public Category          category;
		@JSON.Optional	public BigDecimal        deviation;
		@JSON.Optional	public BigDecimal        dividendYield;
						public Icons             icons;
						public int               isFavorite;
						public ManagementCompany managementCompany;
						public BigDecimal        managementFee;
						public int               marketMake;
		@JSON.Optional	public int               minInvest;
		@JSON.Optional	public long              netAssets;
						public String            stockCode;
						public String            stockName;
						public String            targetIndex;
	}
	
	public String status;
	public Data[] data;
	
    @Override
    public String toString() {
        return ToString.withFieldName(this);
    }
    
    public static String download() {
		var body        = "{\"uid\":\"" + UUID.randomUUID().toString() + "\"}";
		var contentType = "application/json;charset=UTF-8";
		var url         = "https://jpx.cloud.qri.jp/tosyo-moneybu/api/list/etf";
		var string      = HttpUtil.getInstance().withPost(body, contentType).downloadString(url);
		
		return string;
    }
}