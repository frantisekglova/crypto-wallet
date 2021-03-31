package sk.glova.cryptowallet.services.impl;

import static org.springframework.http.HttpMethod.GET;
import static sk.glova.cryptowallet.utils.Helper.getStringFromEnums;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.glova.cryptowallet.domain.CryptoCurrencyCode;
import sk.glova.cryptowallet.domain.CurrencyCode;
import sk.glova.cryptowallet.domain.model.CryptoCurrencyRate;
import sk.glova.cryptowallet.services.api.RateService;

@Service
@RequiredArgsConstructor
public class RateServiceImpl implements RateService {

    private final RestTemplate restTemplate;
    @Value("${external.api.multi-url}")
    private String url;

    @Override
    public Page<CryptoCurrencyRate> getRates(Pageable pageable) {
        // Fetch map from external api
        Map<String, Map<String, BigDecimal>> map = getMapFromApi();

        // Prepare for pagination
        final int start = (int) pageable.getOffset();
        assert map != null;
        final int end = Math.min((start + pageable.getPageSize()), map.size());

        // Transform map of currencies into list of objects CryptoCurrencyRate
        final List<CryptoCurrencyRate> cryptoCurrencyRates = transformToListOfRates(map);

        // Sort if it is declared in Pageable object
        pageable.getSort().forEach(order -> sort(cryptoCurrencyRates, order));

        // Paginate list
        return new PageImpl<>(cryptoCurrencyRates.subList(start, end), pageable, cryptoCurrencyRates.size());
    }

    private Map<String, Map<String, BigDecimal>> getMapFromApi() {
        final String fsyms = getStringFromEnums(CryptoCurrencyCode.class, ",");
        final String tsyms = getStringFromEnums(CurrencyCode.class, ",");

        return restTemplate
            .exchange(url, GET, null, new ParameterizedTypeReference<Map<String, Map<String, BigDecimal>>>() {}, fsyms, tsyms)
            .getBody();
    }

    private List<CryptoCurrencyRate> transformToListOfRates(Map<String, Map<String, BigDecimal>> map) {
        return map.keySet()
            .stream()
            .map(key -> CryptoCurrencyRate.builder()
                .name(key)
                .currencyRates(map.get(key))
                .build())
            .collect(Collectors.toList());
    }

    private void sort(List<CryptoCurrencyRate> cryptoCurrencyRates, Sort.Order order) {
        cryptoCurrencyRates.sort(new CryptoCurrencyRate.Comparator(order.getProperty(), order.getDirection().name()));
    }

}
