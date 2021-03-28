package sk.glova.cryptowallet.controllers;

import static org.springframework.http.HttpStatus.OK;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sk.glova.cryptowallet.domain.model.CryptoCurrencyRate;
import sk.glova.cryptowallet.services.RateServiceImpl;

@RestController
@RequestMapping(value = "rest/v1/rate", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RateController {

    final private RateServiceImpl service;

    @Operation(description = "Returns all supported cryptocurrencies with their rates. Result is paginated with sorting capabilities.")
    @PageableAsQueryParam
    @ResponseStatus(OK)
    @GetMapping("/getRates")
    public Page<CryptoCurrencyRate> getRates(@Parameter(hidden = true) Pageable pageable) {
        return service.getRates(pageable);
    }

}
