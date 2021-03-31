package sk.glova.cryptowallet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.glova.cryptowallet.domain.model.SupportedCurrency;

public interface CurrencyRepository extends JpaRepository<SupportedCurrency, Long> {

}
