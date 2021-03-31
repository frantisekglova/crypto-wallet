package sk.glova.cryptowallet.services.impl;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import sk.glova.cryptowallet.dao.CurrencyRepository;
import sk.glova.cryptowallet.domain.model.SupportedCurrency;
import sk.glova.cryptowallet.services.api.CurrencyService;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Override
    @Cacheable("currencies")
    public List<SupportedCurrency> getAllSupportedCurrencies() {
        return currencyRepository.findAll()
            .stream()
            .filter(cur -> !cur.isCryptoCurrency())
            .collect(Collectors.toList());
    }

    @Override
    @Cacheable("crypto-currencies")
    public List<SupportedCurrency> getAllSupportedCryptoCurrencies() {
        return currencyRepository.findAll()
            .stream()
            .filter(SupportedCurrency::isCryptoCurrency)
            .collect(Collectors.toList());
    }

}
