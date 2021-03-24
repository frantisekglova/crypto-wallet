package sk.glova.cryptowallet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.glova.cryptowallet.model.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

}
