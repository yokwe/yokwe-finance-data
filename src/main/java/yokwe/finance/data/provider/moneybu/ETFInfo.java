package yokwe.finance.data.provider.moneybu;

import yokwe.util.ToString;

public class ETFInfo implements Comparable<ETFInfo> {
	public enum Type {
		ETF,
		ETN,
	}
	
	public String  stockCode;
	public Type    type;      // ETF or ETN
	public String  category;
	public String  name;


	public ETFInfo(String stockCode, Type type, String category,String name) {
		this.stockCode     = stockCode;
		this.type          = type;
		this.category      = category;
		this.name          = name;
	}
	public ETFInfo() {}
	
	public boolean isETF() {
		return type.equals("ETF");
	}
	public boolean isETN() {
		return type.equals("ETN");
	}
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}

	@Override
	public int compareTo(ETFInfo that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof ETFInfo) {
			var that = (ETFInfo)o;
			return
				this.stockCode.equals(that.stockCode) &&
				this.type.equals(that.type) &&
				this.category.equals(that.category) &&
				this.name.equals(that.name);
		}
		return false;
	}
}
