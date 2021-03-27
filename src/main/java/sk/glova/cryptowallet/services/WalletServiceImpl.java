package sk.glova.cryptowallet.services;

import static sk.glova.cryptowallet.utils.Helper.getStringFromEnums;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import sk.glova.cryptowallet.dao.WalletRepository;
import sk.glova.cryptowallet.domain.CryptoCurrencyCode;
import sk.glova.cryptowallet.domain.CurrencyCode;
import sk.glova.cryptowallet.domain.model.CryptoCurrencyRate;
import sk.glova.cryptowallet.domain.model.Currency;
import sk.glova.cryptowallet.domain.model.Wallet;
import sk.glova.cryptowallet.domain.request.AddRequest;
import sk.glova.cryptowallet.domain.request.TransferRequest;
import sk.glova.cryptowallet.domain.request.UpsertWalletRequest;
import sk.glova.cryptowallet.exception.EntityNotFoundException;
import sk.glova.cryptowallet.exception.OperationNotAllowedException;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private static final String MULTI_URL = "https://min-api.cryptocompare.com/data/pricemulti?fsyms={fsyms}&tsyms={tsyms}";
    private static final String SINGLE_URL = "https://min-api.cryptocompare.com/data/price?fsym={fsym}&tsyms={tsyms}";

    private final RestTemplate restTemplate;
    private final WalletRepository walletRepository;

    @Override
    public Page<CryptoCurrencyRate> getAllCryptoCurrencies(Pageable pageable) {
        final String fsyms = getStringFromEnums(CryptoCurrencyCode.class, ",");
        final String tsyms = getStringFromEnums(CurrencyCode.class, ",");

        Map<String, Map<String, BigDecimal>> map = exchangeAsMap(
            MULTI_URL,
            new ParameterizedTypeReference<>() {
            },
            Map.of("fsyms", fsyms, "tsyms", tsyms)
        );
        final int start = (int) pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), map.size());

        final List<CryptoCurrencyRate> cryptoCurrencyRates = map.keySet()
            .stream()
            .map(key -> CryptoCurrencyRate.builder().name(key).currencyRates(map.get(key)).build())
            .collect(Collectors.toList());

        return new PageImpl<>(cryptoCurrencyRates.subList(start, end), pageable, cryptoCurrencyRates.size());
    }

    @Override
    @Transactional
    public Wallet createWallet(UpsertWalletRequest request) {
        // this isn't necessary, but for this use case when we dont have owner I want to have unique names.
        checkName(request.getName());

        Wallet wallet = Wallet.builder()
            .name(request.getName())
            .currencies(new HashSet<>())
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
    @Transactional(readOnly = true)
    public Wallet getWallet(Long walletId) {
        return findByIdOrThrow(walletId);
    }

    @Override
    @Transactional
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

        checkCurrency(walletCurrency, CryptoCurrencyCode.class);
        checkCurrency(inputCurrency, CurrencyCode.class);

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
        checkCurrency(currencyTo, CryptoCurrencyCode.class);
        checkCurrency(currencyFrom, CryptoCurrencyCode.class);

        // check whether Wallet has open Account with specified currency
        final Currency currency = walletFrom.getCurrencies().stream()
            .filter(code -> code.getCode().equals(currencyFrom))
            .findAny()
            .orElseThrow(() -> new OperationNotAllowedException("Wallet dont have open account with specified currency."));

        final BigDecimal oldAmount = currency.getAmount();
        final BigDecimal amountForTransfer = transferRequest.getAmount();

        // check whether there is enough money for transaction
        if (oldAmount.compareTo(amountForTransfer) < 0) {
            throw new OperationNotAllowedException("Wallet dont have enough money in account with specified currency.");
        }

        // subtract money from walletFrom
        currency.setAmount(oldAmount.subtract(amountForTransfer));

        // add money to walletTo
        final BigDecimal rate = getConversionRate(currencyFrom, currencyTo);
        final BigDecimal addition = transferRequest.getAmount().multiply(rate);
        doAdd(walletTo, currencyTo, addition);
    }

    private void doAdd(Wallet wallet, String walletCurrency, BigDecimal addition) {
        final Optional<Currency> any = wallet.getCurrencies().stream()
            .filter(currency -> currency.getCode().equals(walletCurrency))
            .findAny();

        if (any.isPresent()) {
            Currency currency = any.get();
            BigDecimal amount = currency.getAmount();
            currency.setAmount(amount.add(addition));
        } else {
            Currency currency = Currency.builder()
                .code(walletCurrency)
                .amount(addition)
                .build();
            wallet.addCurrency(currency);
        }
    }

    private void checkWalletExist(Long walletId) {
        if (!walletRepository.existsById(walletId)) {
            throw new EntityNotFoundException("Wallet with given ID does not exist");
        }
    }

    private void checkName(String name) {
        if (walletRepository.findByName(name).isPresent()) {
            throw new OperationNotAllowedException("Wallet name already exist.");
        }
    }

    private <E extends Enum<E>> void checkCurrency(String currency, Class<E> clazz) {
        String sanitizedCurrency = sanitizeCurrency(currency);

        final boolean isNotSupported = EnumSet.allOf(clazz)
            .stream()
            .map(Enum::toString)
            .noneMatch(curr -> curr.equals(sanitizedCurrency));

        if (isNotSupported) {
            throw new OperationNotAllowedException("Currency [" + sanitizedCurrency + "] is not supported.");
        }
    }

    private String sanitizeCurrency(String currency) {
        return currency.toUpperCase();
    }

    private Wallet findByIdOrThrow(Long walletId) {
        return walletRepository.findById(walletId)
            .orElseThrow(() -> new EntityNotFoundException("Wallet with given ID does not exist"));
    }

    private boolean isAccepted(String newValue, String oldValue) {
        return newValue != null && newValue.length() > 0 && !Objects.equals(newValue, oldValue);
    }

    private BigDecimal getConversionRate(String from, String to) {
        final Map<String, BigDecimal> map = exchangeAsMap(
            SINGLE_URL,
            new ParameterizedTypeReference<>() {
            },
            Map.of("fsym", from, "tsyms", to)
        );

        return map.get(to);
    }

    public <V, K> Map<V, K> exchangeAsMap(String uri, ParameterizedTypeReference<Map<V, K>> responseType, Map<String, String> uriParams) {
        return restTemplate.exchange(uri, HttpMethod.GET, null, responseType, uriParams).getBody();
    }

}
