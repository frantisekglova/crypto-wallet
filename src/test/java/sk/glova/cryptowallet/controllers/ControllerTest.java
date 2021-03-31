package sk.glova.cryptowallet.controllers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Matcher;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import sk.glova.cryptowallet.domain.model.Wallet;
import sk.glova.cryptowallet.domain.request.AddRequest;
import sk.glova.cryptowallet.domain.request.TransferRequest;
import sk.glova.cryptowallet.domain.request.UpsertWalletRequest;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
public abstract class ControllerTest {

    protected static final String WALLET_NAME = "Wallet";
    protected static final String WALLET_URL = "/rest/v1/wallet/";
    protected static final String RATE_URL = "/rest/v1/rate/";
    protected static final String ADD = "/add/";
    protected static final String TRANSFER = "/transfer/";
    protected static final String NOT_SUPPORTED_CUR = "HELLO";
    protected static final String TRANSFER_FROM_CUR = "BTC";
    protected static final String TRANSFER_TO_CUR = "ETH";
    protected static final String ADD_FROM_CUR = "EUR";
    protected static final String ADD_TO_CUR = "BTC";
    protected static final BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(100000);

    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;

    protected TransferRequest getTransferRequest(Long walletId2) {
        return getTransferRequest(walletId2, TRANSFER_FROM_CUR, BigDecimal.ONE);
    }

    protected TransferRequest getTransferRequest(Long walletId2, String currencyFrom) {
        return getTransferRequest(walletId2, currencyFrom, DEFAULT_AMOUNT);
    }

    protected TransferRequest getTransferRequest(Long walletId2, BigDecimal amount) {
        return getTransferRequest(walletId2, TRANSFER_FROM_CUR, amount);
    }

    protected TransferRequest getTransferRequest(Long walletId2, String currencyFrom, BigDecimal amount) {
        return TransferRequest.builder()
            .cryptoCurrencyFrom(currencyFrom)
            .amount(amount)
            .cryptoCurrencyTo(TRANSFER_TO_CUR)
            .destinationWalletId(walletId2)
            .build();
    }

    protected AddRequest getAddRequest() {
        return getAddRequest(ADD_FROM_CUR);
    }

    protected AddRequest getAddRequest(String currencyFrom) {
        return AddRequest.builder()
            .fiatCurrencyFrom(currencyFrom)
            .amount(DEFAULT_AMOUNT)
            .cryptoCurrencyTo(ADD_TO_CUR)
            .build();
    }

    protected void addIntoWallet(Long walletId1) throws Exception {
        final AddRequest addRequest = AddRequest.builder()
            .fiatCurrencyFrom(ADD_FROM_CUR)
            .amount(DEFAULT_AMOUNT)
            .cryptoCurrencyTo(ADD_TO_CUR)
            .build();

        call(POST, WALLET_URL + walletId1 + ADD, addRequest);
    }

    protected void checkEntityNotFoundException(ResultActions result) throws Exception {
        result
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status", is("404 NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Wallet with given ID does not exist.")))
            .andExpect(jsonPath("$.exception", is("EntityNotFoundException")))
            .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    protected void checkOperationNotAllowedException(ResultActions result, String message) throws Exception {
        result
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status", is("422 UNPROCESSABLE_ENTITY")))
            .andExpect(jsonPath("$.message", is(message)))
            .andExpect(jsonPath("$.exception", is("OperationNotAllowedException")))
            .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    protected void checkResult(ResultActions result, ResultMatcher status, Matcher<?> name, Matcher<?> currencies) throws Exception {
        result
            .andExpect(status)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.name", name))
            .andExpect(jsonPath("$.currencies", currencies));
    }

    protected void checkResultWithCurrencies(ResultActions result, Matcher<?> name, Matcher<?> code, Matcher<?> amount) throws Exception {
        result
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.name", name))
            .andExpect(jsonPath("$.currencies[0].code", code))
            .andExpect(jsonPath("$.currencies[0].amount", amount));
    }

    protected Long createWalletAndReturnId(String walletName) throws Exception {
        final UpsertWalletRequest createRequest = UpsertWalletRequest.builder().name(walletName).build();

        final String content = call(POST, WALLET_URL, createRequest)
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readValue(content, Wallet.class).getId();
    }

    protected ResultActions call(HttpMethod httpMethod, String url) throws Exception {
        return call(httpMethod, url, null);
    }

    protected ResultActions call(HttpMethod httpMethod, String url, Object request) throws Exception {
        final MockHttpServletRequestBuilder builder = request(httpMethod, url, request);

        if (request != null) {
            builder
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));
        }

        return mockMvc.perform(builder);
    }

}
