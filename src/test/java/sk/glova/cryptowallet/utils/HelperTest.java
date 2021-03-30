package sk.glova.cryptowallet.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import sk.glova.cryptowallet.domain.CryptoCurrencyCode;

@SpringBootTest
class HelperTest {

    @Test
    void getStringFromEnums() {
        final String expected = "BTC, ETH, LTC, ADA, DOT, BCH, XLM, BNB, USDT, XMR";
        final String actual = Helper.getStringFromEnums(CryptoCurrencyCode.class, ", ");
        assertEquals(expected, actual);
    }

}