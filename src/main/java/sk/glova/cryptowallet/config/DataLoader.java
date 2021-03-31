package sk.glova.cryptowallet.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import sk.glova.cryptowallet.dao.CurrencyRepository;
import sk.glova.cryptowallet.domain.model.SupportedCurrency;

@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final CurrencyRepository currencyRepository;

    // Creates supported currencies and crypto-currencies in DB
    public void run(ApplicationArguments args) {
        currencyRepository.save(SupportedCurrency.builder().code("BTC").isCryptoCurrency(true).build());
        currencyRepository.save(SupportedCurrency.builder().code("ETH").isCryptoCurrency(true).build());
        currencyRepository.save(SupportedCurrency.builder().code("LTC").isCryptoCurrency(true).build());
        currencyRepository.save(SupportedCurrency.builder().code("ADA").isCryptoCurrency(true).build());
        currencyRepository.save(SupportedCurrency.builder().code("DOT").isCryptoCurrency(true).build());
        currencyRepository.save(SupportedCurrency.builder().code("BCH").isCryptoCurrency(true).build());
        currencyRepository.save(SupportedCurrency.builder().code("XLM").isCryptoCurrency(true).build());
        currencyRepository.save(SupportedCurrency.builder().code("BNB").isCryptoCurrency(true).build());
        currencyRepository.save(SupportedCurrency.builder().code("USDT").isCryptoCurrency(true).build());
        currencyRepository.save(SupportedCurrency.builder().code("XMR").isCryptoCurrency(true).build());
        currencyRepository.save(SupportedCurrency.builder().code("USD").isCryptoCurrency(false).build());
        currencyRepository.save(SupportedCurrency.builder().code("EUR").isCryptoCurrency(false).build());
        currencyRepository.save(SupportedCurrency.builder().code("AUD").isCryptoCurrency(false).build());
        currencyRepository.save(SupportedCurrency.builder().code("CZK").isCryptoCurrency(false).build());
        currencyRepository.save(SupportedCurrency.builder().code("JPY").isCryptoCurrency(false).build());
        currencyRepository.save(SupportedCurrency.builder().code("RUB").isCryptoCurrency(false).build());
        currencyRepository.save(SupportedCurrency.builder().code("CNY").isCryptoCurrency(false).build());
        currencyRepository.save(SupportedCurrency.builder().code("HRK").isCryptoCurrency(false).build());
        currencyRepository.save(SupportedCurrency.builder().code("PLN").isCryptoCurrency(false).build());
        currencyRepository.save(SupportedCurrency.builder().code("CHF").isCryptoCurrency(false).build());
    }

}
