package sk.glova.cryptowallet.services;

import static sk.glova.cryptowallet.utils.Helper.getStringFromEnums;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.glova.cryptowallet.dao.WalletRepository;
import sk.glova.cryptowallet.domain.CryptoCurrency;
import sk.glova.cryptowallet.domain.Currency;
import sk.glova.cryptowallet.domain.model.Account;
import sk.glova.cryptowallet.domain.model.Wallet;
import sk.glova.cryptowallet.domain.request.AddRequest;
import sk.glova.cryptowallet.domain.request.TransferRequest;
import sk.glova.cryptowallet.domain.request.UpsertWalletRequest;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private static final String MULTI_URL = "https://min-api.cryptocompare.com/data/pricemulti?fsyms={fsyms}&tsyms={tsyms}";
    private static final String SINGLE_URL = "https://min-api.cryptocompare.com/data/price?fsym={fsym}&tsyms={tsyms}";

    private final RestTemplate restTemplate = new RestTemplate();
    private final WalletRepository walletRepository;

    @Override
    public Object getAllCryptoCurrencies() {
        final String fsyms = getStringFromEnums(CryptoCurrency.class, ",");
        final String tsyms = getStringFromEnums(Currency.class, ",");

        return restTemplate.getForObject(
            MULTI_URL,
            Object.class,
            Map.of("fsyms", fsyms, "tsyms", tsyms)
        );
    }

    @Override
    public Wallet createWallet(UpsertWalletRequest request) {
        // this isn't necessary, but for this use case when we dont have owner I want to have unique names.
        checkName(request.getName());

        Wallet wallet = Wallet.builder()
            .name(request.getName())
            .accounts(new HashSet<>())
            .build();

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void updateWallet(Long walletId, UpsertWalletRequest request) {
        final Wallet wallet = findByIdOrThrow(walletId);

        final String name = request.getName();
        if (isAccepted(name, wallet.getName())) {
            wallet.setName(name);
        }
    }

    @Override
    public Wallet getWallet(Long walletId) {
        return findByIdOrThrow(walletId);
    }

    @Override
    public void deleteWallet(Long walletId) {
        checkWalletExist(walletId);

        walletRepository.deleteById(walletId);
    }

    @Override
    @Transactional
    public void add(Long walletId, AddRequest addRequest) {
        final Wallet wallet = findByIdOrThrow(walletId);

        final String walletCurrency = sanitizeCurrency(addRequest.getCurrencyInWallet());
        final String inputCurrency = sanitizeCurrency(addRequest.getCurrencyInInput());

        checkCurrency(walletCurrency, CryptoCurrency.class);
        checkCurrency(inputCurrency, Currency.class);

        final BigDecimal rate = getConversionRate(inputCurrency, walletCurrency);
        final BigDecimal addition = addRequest.getAmount().multiply(rate);

        // find out whether an account in specified currency already exists in wallet, if no create new
        doAdd(wallet, walletCurrency, addition);
    }

    @Override
    @Transactional
    public void transfer(Long walletId, TransferRequest transferRequest) {
        // check whether both Wallets exists
        final Wallet walletFrom = findByIdOrThrow(walletId);
        final Wallet walletTo = findByIdOrThrow(transferRequest.getDestWalletId());

        final String currencyFrom = sanitizeCurrency(transferRequest.getCurrencyFrom());
        final String currencyTo = sanitizeCurrency(transferRequest.getCurrencyTo());

        // check whether there are specified supported currencies
        checkCurrency(currencyTo, CryptoCurrency.class);
        checkCurrency(currencyFrom, CryptoCurrency.class);

        // check whether Wallet has open Account with specified currency
        final Account account = walletFrom.getAccounts().stream()
            .filter(acc -> acc.getCurrency().equals(currencyFrom))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Wallet dont have open account with specified currency."));

        final BigDecimal oldAmount = account.getAmount();
        final BigDecimal amountForTransfer = transferRequest.getAmount();

        // check whether there is enough money for transaction
        if (oldAmount.compareTo(amountForTransfer) < 0) {
            throw new IllegalStateException("Wallet dont have enough money in account with specified currency.");
        }

        // subtract money from walletFrom
        account.setAmount(oldAmount.subtract(amountForTransfer));

        // add money to walletTo
        final BigDecimal rate = getConversionRate(currencyFrom, currencyTo);
        final BigDecimal addition = transferRequest.getAmount().multiply(rate);
        doAdd(walletTo, currencyTo, addition);
    }

    private void doAdd(Wallet wallet, String walletCurrency, BigDecimal addition) {
        final Optional<Account> any = wallet.getAccounts().stream()
            .filter(account -> account.getCurrency().equals(walletCurrency))
            .findAny();

        if (any.isPresent()) {
            Account account = any.get();
            BigDecimal amount = account.getAmount();
            account.setAmount(amount.add(addition));
        } else {
            Account account = Account.builder()
                .currency(walletCurrency)
                .amount(addition)
                .build();
            wallet.addAccount(account);
        }
    }

    private void checkWalletExist(Long walletId) {
        if (!walletRepository.existsById(walletId)) {
            throw new IllegalStateException("Wallet with given ID does not exist");
        }
    }

    private void checkName(String name) {
        if (walletRepository.findByName(name).isPresent()) {
            throw new IllegalStateException("Wallet name already exist.");
        }
    }

    private <E extends Enum<E>> void checkCurrency(String currency, Class<E> clazz) {
        String sanitizedCurrency = sanitizeCurrency(currency);

        final boolean isNotSupported = EnumSet.allOf(clazz)
            .stream()
            .map(Enum::toString)
            .noneMatch(curr -> curr.equals(sanitizedCurrency));

        if (isNotSupported) {
            throw new IllegalStateException("Currency [" + sanitizedCurrency + "] is not supported.");
        }
    }

    private String sanitizeCurrency(String currency) {
        return currency.toUpperCase();
    }

    private Wallet findByIdOrThrow(Long walletId) {
        return walletRepository.findById(walletId)
            .orElseThrow(() -> new IllegalStateException("Wallet with given ID does not exist"));
    }

    private boolean isAccepted(String newValue, String oldValue) {
        return newValue != null && newValue.length() > 0 && !Objects.equals(newValue, oldValue);
    }

    private BigDecimal getConversionRate(String from, String to) {
        final Object object = restTemplate.getForObject(
            SINGLE_URL,
            Object.class,
            Map.of("fsym", from, "tsyms", to)
        );

        @SuppressWarnings("unchecked") // I am sure what output will be from external API
        LinkedHashMap<String, Double> obj = (LinkedHashMap<String, Double>) object;
        assert obj != null;
        return BigDecimal.valueOf(obj.get(to));
    }

}
