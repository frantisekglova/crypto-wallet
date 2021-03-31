package sk.glova.cryptowallet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ExternalApiConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
