package yokwe.finance.data.provider.moneybu.api;

import yokwe.util.ToString;

public final class Category {
	public int    categoryCode;
	public String categoryName;
	public int    orderNum;
	
    @Override
    public String toString() {
        return ToString.withFieldName(this);
    }
}