package yokwe.finance.data.provider.moneybu.api;

import yokwe.util.ToString;

public final class ManagementCompany {
    public String code;
    public String name;
    public String url;

    @Override
    public String toString() {
        return ToString.withFieldName(this);
    }
}