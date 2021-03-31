package sk.glova.cryptowallet.config;

import java.time.LocalDateTime;
import javax.management.timer.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class CachingConfig {

    public static final String SUPPORTED_CURRENCIES = "currencies";
    public static final String SUPPORTED_CRYPTO_CURRENCIES = "crypto-currencies";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(SUPPORTED_CURRENCIES, SUPPORTED_CRYPTO_CURRENCIES);
    }

    @CacheEvict(allEntries = true, value = {SUPPORTED_CURRENCIES, SUPPORTED_CRYPTO_CURRENCIES})
    @Scheduled(fixedRate = Timer.ONE_HOUR * 24) // scheduled every day (24 hours)
    public void reportCacheEvict() {
        log.info("Flush Cache " + LocalDateTime.now());
    }

}
