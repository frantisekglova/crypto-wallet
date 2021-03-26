package sk.glova.cryptowallet.domain.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    // TODO better name than Account
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Currency> currencies = new HashSet<>();

    public void addCurrency(Currency currency) {
        currencies.add(currency);
        currency.setWallet(this);
    }

}
