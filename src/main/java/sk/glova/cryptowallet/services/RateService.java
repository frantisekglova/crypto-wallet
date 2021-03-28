package sk.glova.cryptowallet.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.glova.cryptowallet.domain.model.CryptoCurrencyRate;

public interface RateService {

    Page<CryptoCurrencyRate> getRates(Pageable pageable);

}
