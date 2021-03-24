package sk.glova.cryptowallet.services;

import static sk.glova.cryptowallet.utils.Helper.getStringFromEnums;

import java.util.EnumSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.glova.cryptowallet.dao.WalletRepository;
import sk.glova.cryptowallet.exception.OperationNotAllowedException;
import sk.glova.cryptowallet.model.CryptoCurrency;
import sk.glova.cryptowallet.model.Currency;
import sk.glova.cryptowallet.model.Wallet;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final WalletRepository walletRepository;

    public Object getAllCryptoCurrencies() {
        final String fsyms = getStringFromEnums(CryptoCurrency.class, ",");
        final String tsyms = getStringFromEnums(Currency.class, ",");

        return restTemplate.getForObject(
            "https://min-api.cryptocompare.com/data/pricemulti?fsyms={fsyms}&tsyms={tsyms}",
            Object.class,
            Map.of("fsyms", fsyms, "tsyms", tsyms)
        );
    }

    public Wallet createWallet(Wallet createRequest) {
        if (!isCurrencySupported(createRequest.getCurrency())) {
            throw new OperationNotAllowedException("Currency is not supported.");
        }
        if (!isNameFree(createRequest.getName())) {
            throw new OperationNotAllowedException("Wallet name already exist.");
        }
        return walletRepository.save(createRequest);
    }

    private boolean isNameFree(String name) {
        return walletRepository.findAll().stream()
            .map(Wallet::getName)
            .anyMatch(n -> n.equals(name));
    }

    private boolean isCurrencySupported(String currency) {
        final String currencyUppercase = currency.toUpperCase();

        return EnumSet.allOf(CryptoCurrency.class)
            .stream()
            .map(Enum::toString)
            .anyMatch(curr -> curr.equals(currencyUppercase));
    }

}
