package sk.glova.cryptowallet.services.api;

import java.util.List;
import sk.glova.cryptowallet.domain.model.SupportedCurrency;

public interface CurrencyService {

    /**
     * Returns all supported currencies. The result is cached.
     * @return list of SupportedCurrency objects
     */
    List<SupportedCurrency> getAllSupportedCurrencies();

    /**
     * Returns all supported crypto-currencies. The result is cached.
     * @return list of SupportedCurrency objects
     */
    List<SupportedCurrency> getAllSupportedCryptoCurrencies();

}
