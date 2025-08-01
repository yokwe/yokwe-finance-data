package yokwe.finance.data.provider.moneybu.api;

import java.math.BigDecimal;

import yokwe.util.ToString;

public final class PcfWeight {
    public String     code;
    public String     name;
    public BigDecimal rank;
    public BigDecimal weight;

    @Override
    public String toString() {
        return ToString.withFieldName(this);
    }
}