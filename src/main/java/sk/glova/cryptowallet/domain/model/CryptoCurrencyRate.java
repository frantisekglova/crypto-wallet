package sk.glova.cryptowallet.domain.model;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CryptoCurrencyRate implements Comparable<CryptoCurrencyRate> {

    String name;
    Map<String, BigDecimal> currencyRates;

    @Override
    public int compareTo(CryptoCurrencyRate o) {
        return this.getName().compareTo(o.getName());
    }

}
