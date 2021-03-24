package sk.glova.cryptowallet.controllers;

import static org.springframework.http.HttpStatus.CREATED;
import static sk.glova.cryptowallet.utils.Helper.getCurrentLocationWithId;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sk.glova.cryptowallet.model.Wallet;
import sk.glova.cryptowallet.services.WalletService;

@RestController
@RequestMapping("rest/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    final private WalletService service;

    @GetMapping("/getAllCryptoCurrencies")
    Object getAllCryptoCurrencies() {
        return service.getAllCryptoCurrencies();
    }

    @ResponseStatus(CREATED)
    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody Wallet createRequest) {
        Wallet savedWallet = service.createWallet(createRequest);
        return ResponseEntity.created(getCurrentLocationWithId(savedWallet.getId())).body(savedWallet);
    }

}
