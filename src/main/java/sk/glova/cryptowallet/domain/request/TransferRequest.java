package sk.glova.cryptowallet.domain.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequest {

    private String currencyFrom;
    private String currencyTo;
    private BigDecimal amount;
    private Long destinationWalletId;

}
