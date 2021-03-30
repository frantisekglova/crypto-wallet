package sk.glova.cryptowallet.services.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.glova.cryptowallet.domain.model.CryptoCurrencyRate;

public interface RateService {

    /**
     * Returns all supported cryptocurrencies with their rates. Result is paginated with sorting capabilities.
     * @param pageable pageable object
     * @return page of cryptoCurrencyRate objects
     */
    Page<CryptoCurrencyRate> getRates(Pageable pageable);

}
