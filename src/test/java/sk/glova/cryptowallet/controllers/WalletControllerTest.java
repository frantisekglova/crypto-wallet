package sk.glova.cryptowallet.controllers;

import static java.lang.Long.MAX_VALUE;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import sk.glova.cryptowallet.domain.model.Currency;
import sk.glova.cryptowallet.domain.request.UpsertWalletRequest;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerTest extends ControllerTest {

    public WalletControllerTest(@Autowired ObjectMapper objectMapper, @Autowired MockMvc mockMvc) {
        super(objectMapper, mockMvc);
    }

    /* ---------------- CREATE WALLET -------------- */

    @Test
    @Transactional
    void givenWalletWithUniqueName_whenCreateWallet_thenCreatedWalletReturned() throws Exception {
        final ResultActions result = call(POST, WALLET_URL, UpsertWalletRequest.builder().name(WALLET_NAME).build());
        checkResult(result, status().isCreated(), is(WALLET_NAME), emptyCollectionOf(Currency.class));
    }

    @Test
    @Transactional
    void givenWalletWithEmptyName_whenCreateWallet_thenOperationNotAllowedExceptionThrown() throws Exception {
        final ResultActions result = call(POST, WALLET_URL, UpsertWalletRequest.builder().name("").build());
        checkOperationNotAllowedException(result, "Provided name was null or empty string.");
    }

    @Test
    @Transactional
    void givenWalletWithDuplicatedName_whenCreateWallet_thenOperationNotAllowedExceptionThrown() throws Exception {
        // create a wallet
        call(POST, WALLET_URL, UpsertWalletRequest.builder().name(WALLET_NAME).build());

        // create again wallet with the same name
        final ResultActions result = call(POST, WALLET_URL, UpsertWalletRequest.builder().name(WALLET_NAME).build());
        checkOperationNotAllowedException(result, "Wallet name already exist.");
    }

    /* ---------------- UPDATE WALLET -------------- */

    @Test
    @Transactional
    void givenExistingWalletId_whenUpdateWallet_thenUpdatedWalletReturned() throws Exception {
        // create a wallet and get its ID
        final Long walletId = createWalletAndReturnId(WALLET_NAME);

        // update created wallet
        final String nameUpdate = WALLET_NAME + 1;
        final ResultActions result = call(PUT, WALLET_URL + walletId, UpsertWalletRequest.builder().name(nameUpdate).build());
        checkResult(result, status().isOk(), is(nameUpdate), emptyCollectionOf(Currency.class));
    }

    @Test
    @Transactional
    void givenNotExistingWalletId_whenUpdateWallet_thenEntityNotFoundExceptionThrown() throws Exception {
        final ResultActions result = call(PUT, WALLET_URL + MAX_VALUE, UpsertWalletRequest.builder().name(WALLET_NAME).build());
        checkEntityNotFoundException(result);
    }

    /* ---------------- GET WALLET/WALLETS -------------- */

    @Test
    @Transactional
    void givenExistingWalletID_whenGetWallet_thenWalletReturned() throws Exception {
        // create a wallet and get its ID
        final Long walletId = createWalletAndReturnId(WALLET_NAME);

        // get created wallet
        final ResultActions result = call(GET, WALLET_URL + walletId);
        checkResult(result, status().isOk(), is(WALLET_NAME), emptyCollectionOf(Currency.class));
    }

    @Test
    @Transactional
    void givenNotExistingWalletID_whenGetWallet_thenEntityNotFoundExceptionThrown() throws Exception {
        // get not existing wallet
        final ResultActions result = call(GET, WALLET_URL + MAX_VALUE);
        checkEntityNotFoundException(result);
    }

    @Test
    @Transactional
    void whenGetWallets_thenWalletsReturned() throws Exception {
        // create two wallets
        call(POST, WALLET_URL, UpsertWalletRequest.builder().name(WALLET_NAME).build());
        call(POST, WALLET_URL, UpsertWalletRequest.builder().name(WALLET_NAME + 1).build());

        // get created wallets
        call(GET, WALLET_URL)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$['pageable']['paged']").value(true))
            .andExpect(jsonPath("$.content", hasSize(2)));
    }

    /* ---------------- DELETE WALLET -------------- */

    @Test
    @Transactional
    void givenExistingWalletId_whenDeleteWallet_thenDeletedWalletReturned() throws Exception {
        // create a wallet and get its ID
        final Long walletId = createWalletAndReturnId(WALLET_NAME);

        // delete created wallet
        final ResultActions result = call(DELETE, WALLET_URL + walletId);
        checkResult(result, status().isOk(), is(WALLET_NAME), emptyCollectionOf(Currency.class));
    }

    @Test
    @Transactional
    void givenNotExistingWalletId_whenDeleteWallet_thenEntityNotFoundExceptionReturned() throws Exception {
        // delete not existing wallet
        final ResultActions result = call(DELETE, WALLET_URL + MAX_VALUE);
        checkEntityNotFoundException(result);
    }

    /* ---------------- ADD INTO WALLET -------------- */

    @Test
    @Transactional
    void givenCorrectAddRequest_whenAdd_thenWalletWithAddedCurrencyReturned() throws Exception {
        // create a wallet and get its ID
        final Long walletId = createWalletAndReturnId(WALLET_NAME);

        // add into created wallet
        final ResultActions result = call(POST, WALLET_URL + walletId + ADD, getAddRequest());
        checkResultWithCurrencies(result, is(WALLET_NAME), is(ADD_TO_CUR), notNullValue());
    }

    @Test
    @Transactional
    void givenAddRequestWithNotSupportedCurrency_whenAdd_thenOperationNotAllowedExceptionReturned() throws Exception {
        // create a wallet and get its ID
        final Long walletId = createWalletAndReturnId(WALLET_NAME);

        // add into created wallet
        final ResultActions result = call(POST, WALLET_URL + walletId + ADD, getAddRequest(NOT_SUPPORTED_CUR));
        checkOperationNotAllowedException(result, "Currency [" + NOT_SUPPORTED_CUR + "] is not supported.");
    }

    @Test
    @Transactional
    void givenNotExistingWalletId_whenAdd_thenEntityNotFoundExceptionReturned() throws Exception {
        // add into created wallet
        final ResultActions result = call(POST, WALLET_URL + MAX_VALUE + ADD, getAddRequest());
        checkEntityNotFoundException(result);
    }

    /* ---------------- TRANSFER FROM WALLET -------------- */

    @Test
    @Transactional
    void givenTransferRequest_whenTransfer_thenCurrenciesAreTransferFromOneWalletToAnotherOne() throws Exception {
        // create wallets and get their IDs
        final Long walletId1 = createWalletAndReturnId(WALLET_NAME + 1);
        final Long walletId2 = createWalletAndReturnId(WALLET_NAME + 2);

        // add into created wallet 1
        addIntoWallet(walletId1);

        // transfer from created wallet 1 and check whether the amount was subtracted
        final ResultActions result1 = call(POST, WALLET_URL + walletId1 + TRANSFER, getTransferRequest(walletId2));
        checkResultWithCurrencies(result1, is(WALLET_NAME + 1), is(TRANSFER_FROM_CUR), lessThan(DEFAULT_AMOUNT.doubleValue()));

        // check whether in wallet 2 was added amount in given currency
        final ResultActions result2 = call(GET, WALLET_URL + walletId2);
        checkResultWithCurrencies(result2, is(WALLET_NAME + 2), is(TRANSFER_TO_CUR), notNullValue());
    }

    @Test
    @Transactional
    void givenTransferRequestWithWrongCurrency_whenTransfer_thenOperationNotAllowedExceptionReturned() throws Exception {
        // create wallets and get their IDs
        final Long walletId1 = createWalletAndReturnId(WALLET_NAME + 1);
        final Long walletId2 = createWalletAndReturnId(WALLET_NAME + 2);

        // add into created wallet 1
        addIntoWallet(walletId1);

        // transfer from created wallet 1
        final ResultActions result = call(POST, WALLET_URL + walletId1 + TRANSFER, getTransferRequest(walletId2, NOT_SUPPORTED_CUR));
        checkOperationNotAllowedException(result, "Currency [" + NOT_SUPPORTED_CUR + "] is not supported.");
    }

    @Test
    @Transactional
    void givenTransferRequestForNotCreatedWallet_whenTransfer_thenEntityNotFoundExceptionReturned() throws Exception {
        // create wallet and get its ID
        final Long wallet = createWalletAndReturnId(WALLET_NAME + 1);

        // add into created wallet 1
        addIntoWallet(wallet);

        // transfer from created wallet
        final ResultActions result = call(POST, WALLET_URL + wallet + TRANSFER, getTransferRequest(MAX_VALUE));
        checkEntityNotFoundException(result);
    }

    @Test
    @Transactional
    void givenTransferRequestWithoutHaveCurrencyInWallet_whenTransfer_thenOperationNotAllowedExceptionReturned() throws Exception {
        // create wallets and get their IDs
        final Long walletId1 = createWalletAndReturnId(WALLET_NAME + 1);
        final Long walletId2 = createWalletAndReturnId(WALLET_NAME + 2);

        // transfer from created wallet
        final ResultActions result = call(POST, WALLET_URL + walletId1 + TRANSFER, getTransferRequest(walletId2));
        checkOperationNotAllowedException(result, "Wallet does not have specified currency.");
    }

    @Test
    @Transactional
    void givenTransferRequestWithNotEnoughAmountOfCurrency_whenTransfer_thenOperationNotAllowedExceptionReturned() throws Exception {
        // create wallets and get their IDs
        final Long walletId1 = createWalletAndReturnId(WALLET_NAME + 1);
        final Long walletId2 = createWalletAndReturnId(WALLET_NAME + 2);

        // add into created wallet 1
        addIntoWallet(walletId1);

        // transfer from created wallet
        final ResultActions result = call(POST, WALLET_URL + walletId1 + TRANSFER, getTransferRequest(walletId2, BigDecimal.valueOf(MAX_VALUE)));
        checkOperationNotAllowedException(result, "Wallet does not have enough amount in specified currency.");
    }

}