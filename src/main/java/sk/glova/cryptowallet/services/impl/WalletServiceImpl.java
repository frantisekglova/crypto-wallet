package sk.glova.cryptowallet.services.impl;

import static org.springframework.http.HttpMethod.GET;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import sk.glova.cryptowallet.dao.WalletRepository;
import sk.glova.cryptowallet.domain.model.Currency;
import sk.glova.cryptowallet.domain.model.SupportedCurrency;
import sk.glova.cryptowallet.domain.model.Wallet;
import sk.glova.cryptowallet.domain.request.AddRequest;
import sk.glova.cryptowallet.domain.request.TransferRequest;
import sk.glova.cryptowallet.domain.request.UpsertWalletRequest;
import sk.glova.cryptowallet.exception.EntityNotFoundException;
import sk.glova.cryptowallet.exception.OperationNotAllowedException;
import sk.glova.cryptowallet.services.api.CurrencyService;
import sk.glova.cryptowallet.services.api.WalletService;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final RestTemplate restTemplate;
    private final WalletRepository walletRepository;
    private final CurrencyService currencyService;

    @Value("${external.api.single-url}")
    private String url;

    @Override
    @Transactional
    public Wallet createWallet(UpsertWalletRequest request) throws OperationNotAllowedException {
        // this isn't necessary, but this use-case dont have owner in wallet, so name should be unique
        checkName(request.getName());

        Wallet wallet = Wallet.builder()
            .name(request.getName())
            .currencies(new HashSet<>())
            .build();

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void updateWallet(Long walletId, UpsertWalletRequest request) throws OperationNotAllowedException, EntityNotFoundException {
        final Wallet wallet = findByIdOrThrow(walletId);
        final String name = request.getName();

        checkNewName(name, wallet.getName());

        wallet.setName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Wallet getWallet(Long walletId) throws EntityNotFoundException {
        return findByIdOrThrow(walletId);
    }

    @Override
    @Transactional
    public void deleteWallet(Long walletId) throws EntityNotFoundException {
        checkWalletExist(walletId);

        walletRepository.deleteById(walletId);
    }

    @Override
    @Transactional
    public void add(Long walletId, AddRequest addRequest) throws OperationNotAllowedException, EntityNotFoundException {
        final Wallet wallet = findByIdOrThrow(walletId);

        final String walletCurrency = sanitizeCurrency(addRequest.getCryptoCurrencyTo());
        final String inputCurrency = sanitizeCurrency(addRequest.getFiatCurrencyFrom());

        checkCurrency(walletCurrency, currencyService.getAllSupportedCryptoCurrencies());
        checkCurrency(inputCurrency, currencyService.getAllSupportedCurrencies());

        final BigDecimal rate = getConversionRate(inputCurrency, walletCurrency);
        final BigDecimal addition = addRequest.getAmount().multiply(rate);

        doAdd(wallet, walletCurrency, addition);
    }

    @Override
    @Transactional
    public void transfer(Long walletId, TransferRequest transferRequest) throws OperationNotAllowedException, EntityNotFoundException {
        // check whether both Wallets exists
        final Wallet walletFrom = findByIdOrThrow(walletId);
        final Wallet walletTo = findByIdOrThrow(transferRequest.getDestinationWalletId());

        final String currencyFrom = sanitizeCurrency(transferRequest.getCryptoCurrencyFrom());
        final String currencyTo = sanitizeCurrency(transferRequest.getCryptoCurrencyTo());

        // check whether there are specified supported currencies
        checkCurrency(currencyTo, currencyService.getAllSupportedCryptoCurrencies());
        checkCurrency(currencyFrom, currencyService.getAllSupportedCryptoCurrencies());

        // check whether Wallet has open Account with specified currency
        final Currency currency = walletFrom.getCurrencies().stream()
            .filter(code -> code.getCode().equals(currencyFrom))
            .findAny()
            .orElseThrow(() -> new OperationNotAllowedException("Wallet does not have specified currency."));

        final BigDecimal oldAmount = currency.getAmount();
        final BigDecimal amountForTransfer = transferRequest.getAmount();

        // check whether there is enough money for transaction
        if (oldAmount.compareTo(amountForTransfer) < 0) {
            throw new OperationNotAllowedException("Wallet does not have enough amount in specified currency.");
        }

        // subtract amount from walletFrom
        currency.setAmount(oldAmount.subtract(amountForTransfer));

        // add amount to walletTo
        final BigDecimal rate = getConversionRate(currencyFrom, currencyTo);
        final BigDecimal addition = transferRequest.getAmount().multiply(rate);

        doAdd(walletTo, currencyTo, addition);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Wallet> getWallets(Pageable pageable) {
        return walletRepository.findAll(pageable);
    }

    private void doAdd(Wallet wallet, String walletCurrency, BigDecimal addition) {
        // check whether exist currency
        final Optional<Currency> any = wallet.getCurrencies().stream()
            .filter(currency -> currency.getCode().equals(walletCurrency))
            .findAny();

        if (any.isPresent()) {
            // increment existing currency
            Currency currency = any.get();
            BigDecimal amount = currency.getAmount();
            currency.setAmount(amount.add(addition));
        } else {
            // create new currency with specified amount
            Currency currency = Currency.builder()
                .code(walletCurrency)
                .amount(addition)
                .build();
            wallet.addCurrency(currency);
        }
    }

    private void checkWalletExist(Long walletId) throws EntityNotFoundException {
        if (!walletRepository.existsById(walletId)) {
            throw new EntityNotFoundException("Wallet with given ID does not exist.");
        }
    }

    private void checkName(String name) throws OperationNotAllowedException {
        if (name == null || name.length() == 0) {
            throw new OperationNotAllowedException("Provided name was null or empty string.");
        }
        if (walletRepository.findByName(name).isPresent()) {
            throw new OperationNotAllowedException("Wallet name already exist.");
        }
    }

    private void checkCurrency(String currency, List<SupportedCurrency> currencies) throws OperationNotAllowedException {
        final String sanitizedCurrency = sanitizeCurrency(currency);

        final boolean isNotSupported = currencies
            .stream()
            .noneMatch(curr -> curr.getCode().equals(sanitizedCurrency));

        if (isNotSupported) {
            throw new OperationNotAllowedException("Currency [" + sanitizedCurrency + "] is not supported.");
        }
    }

    private String sanitizeCurrency(String currency) {
        return currency.toUpperCase();
    }

    private Wallet findByIdOrThrow(Long walletId) throws EntityNotFoundException {
        return walletRepository.findById(walletId)
            .orElseThrow(() -> new EntityNotFoundException("Wallet with given ID does not exist."));
    }

    private void checkNewName(String newValue, String oldValue) throws OperationNotAllowedException {
        if (Objects.equals(newValue, oldValue)) {
            throw new OperationNotAllowedException("Provided name is the same as it was.");
        }

        checkName(newValue);
    }

    private BigDecimal getConversionRate(String fsym, String tsyms) {
        return Objects.requireNonNull(restTemplate
            .exchange(url, GET, null, new ParameterizedTypeReference<Map<String, BigDecimal>>() {}, fsym, tsyms)
            .getBody())
            .get(tsyms);
    }

}
