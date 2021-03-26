package sk.glova.cryptowallet.domain.model;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CryptoCurrencyRate {

    String name;
    Map<String, BigDecimal> currencyRates;

}
