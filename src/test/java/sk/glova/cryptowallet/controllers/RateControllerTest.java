package sk.glova.cryptowallet.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
public class RateControllerTest extends ControllerTest {

    public RateControllerTest(@Autowired ObjectMapper objectMapper, @Autowired MockMvc mockMvc) {
        super(objectMapper, mockMvc);
    }

    @Test
    @Transactional
    void whenGetRates_thenPagedRatesReturned() throws Exception {
        call(GET, RATE_URL)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$['pageable']['paged']").value(true))
            .andExpect(jsonPath("$.content", hasSize(10)));
    }

    @Test
    @Transactional
    void whenGetRatesSortedAscForName_thenPagedRatesReturned() throws Exception {
        call(GET, RATE_URL + "?page=0&size=1&sort=name")
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$['pageable']['paged']").value(true))
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].name", is("ADA")));
    }

    @Test
    @Transactional
    void whenGetRatesSortedDescForName_thenPagedRatesReturned() throws Exception {
        call(GET, RATE_URL + "?page=0&size=1&sort=name,desc")
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$['pageable']['paged']").value(true))
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].name", is("XMR")));
    }

}
