package yokwe.finance.data.provider.moneybu.api;

import java.math.BigDecimal;

import yokwe.util.ToString;

public final class DividendHist {
    public String     date; // YYYY/MM/DD
    public BigDecimal dividend;
    
    @Override
    public String toString() {
        return ToString.withFieldName(this);
    }
}