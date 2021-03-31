package sk.glova.cryptowallet.services.api;

import javassist.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.glova.cryptowallet.domain.model.Wallet;
import sk.glova.cryptowallet.domain.request.AddRequest;
import sk.glova.cryptowallet.domain.request.TransferRequest;
import sk.glova.cryptowallet.domain.request.UpsertWalletRequest;
import sk.glova.cryptowallet.exception.EntityNotFoundException;
import sk.glova.cryptowallet.exception.OperationNotAllowedException;

//TODO add @NotNull @Positive javax.validation annotations
public interface WalletService {

    /**
     * Creates an empty wallet with the name based on upsert request DTO.
     *
     * @param request Upsert wallet request
     * @return created wallet
     * @throws OperationNotAllowedException when:
     *     <ul>
     *         <li>provided name is already in use</li>
     *         <li>provided name is null or empty string</li>
     *     </ul>
     */
    Wallet createWallet(UpsertWalletRequest request) throws OperationNotAllowedException;

    /**
     * Updates the wallet with given ID. Values are taken from given upsert request DTO.
     *
     * @param walletId wallet ID
     * @param request upsert wallet request
     * @throws OperationNotAllowedException when:
     *     <ul>
     *         <li>provided name is already in use</li>
     *         <li>provided name is the same as it was</li>
     *         <li>provided name is null or empty string</li>
     *     </ul>
     * @throws EntityNotFoundException When wallet with given ID does not exist.
     */
    void updateWallet(Long walletId, UpsertWalletRequest request) throws OperationNotAllowedException, EntityNotFoundException;

    /**
     * Returns the wallet with given ID.
     *
     * @param walletId wallet ID
     * @return object with given ID
     * @throws EntityNotFoundException When wallet with given ID does not exist.
     */
    Wallet getWallet(Long walletId) throws EntityNotFoundException;

    /**
     * Deletes the wallet with given ID.
     *
     * @param walletId wallet ID
     * @throws EntityNotFoundException When wallet with given ID does not exist.
     */
    void deleteWallet(Long walletId) throws EntityNotFoundException;

    /**
     * Adds currency to wallet with given ID. It converts currencyFrom into currencyTo according current rate (if currency exist in wallet - given
     * amount increments the previous one, if does not exist - it creates new one with given amount).
     *
     * @param walletId wallet ID
     * @param addRequest add currency request
     * @throws OperationNotAllowedException When currency or cryptocurrency is not supported.
     * @throws EntityNotFoundException When wallet with given ID does not exist.
     */
    void add(Long walletId, AddRequest addRequest) throws OperationNotAllowedException, EntityNotFoundException;

    /**
     * Transfers currency from wallet with given ID. It converts currencyFrom into currencyTo according current rate. It decrements wallet from which
     * payment is outgoing and increment wallet to which payment is incoming (if currency exist in wallet - given amount increments the previous one,
     * if does not exist - it creates new one with given amount).
     *
     * @param walletId wallet ID
     * @param transferRequest transfer currency request
     * @throws OperationNotAllowedException <ul>
     *     <li>currency or cryptocurrency is not supported</li>
     *     <li>wallet does not have specified currency</li>
     *     <li>wallet does not have enough amount in specified currency</li>
     *     </ul>
     * @throws EntityNotFoundException When wallet with given ID does not exist.
     */
    void transfer(Long walletId, TransferRequest transferRequest) throws NotFoundException, OperationNotAllowedException, EntityNotFoundException;

    /**
     * Returns all wallets. Result is paginated with sorting capabilities.
     *
     * @param pageable pageable object
     * @return page of wallet objects
     */
    Page<Wallet> getWallets(Pageable pageable);

}
