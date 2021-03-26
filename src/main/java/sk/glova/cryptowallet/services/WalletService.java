package sk.glova.cryptowallet.services;

import javassist.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.glova.cryptowallet.domain.model.CryptoCurrencyRate;
import sk.glova.cryptowallet.domain.model.Wallet;
import sk.glova.cryptowallet.domain.request.AddRequest;
import sk.glova.cryptowallet.domain.request.TransferRequest;
import sk.glova.cryptowallet.domain.request.UpsertWalletRequest;

public interface WalletService {

    Page<CryptoCurrencyRate> getAllCryptoCurrencies(Pageable pageable);

    Wallet createWallet(UpsertWalletRequest request);

    void updateWallet(Long walletId, UpsertWalletRequest request) throws NotFoundException;

    Wallet getWallet(Long walletId) throws NotFoundException;

    void deleteWallet(Long walletId) throws NotFoundException;

    void add(Long walletId, AddRequest addRequest) throws NotFoundException;

    void transfer(Long walletId, TransferRequest transferRequest) throws NotFoundException;

}
