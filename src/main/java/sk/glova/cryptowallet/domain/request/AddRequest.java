package sk.glova.cryptowallet.domain.request;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AddRequest {

    private String currencyFrom;
    private String currencyTo;
    private BigDecimal amount;

}
