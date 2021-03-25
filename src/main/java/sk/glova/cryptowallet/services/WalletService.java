package sk.glova.cryptowallet.services;

import javax.transaction.Transactional;
import sk.glova.cryptowallet.domain.request.AddRequest;
import sk.glova.cryptowallet.domain.request.TransferRequest;
import sk.glova.cryptowallet.domain.request.UpsertWalletRequest;
import sk.glova.cryptowallet.domain.model.Wallet;

public interface WalletService {

    Object getAllCryptoCurrencies();

    Wallet createWallet(UpsertWalletRequest request);

    @Transactional
    void updateWallet(Long walletId, UpsertWalletRequest request);

    Wallet getWallet(Long walletId);

    void deleteWallet(Long walletId);

    void add(Long walletId, AddRequest addRequest);

    void transfer(Long walletId, TransferRequest transferRequest);

}
