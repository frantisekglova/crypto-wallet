package sk.glova.cryptowallet.controllers;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static sk.glova.cryptowallet.utils.Helper.getCurrentLocationWithId;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sk.glova.cryptowallet.domain.model.CryptoCurrencyRate;
import sk.glova.cryptowallet.domain.model.Wallet;
import sk.glova.cryptowallet.domain.request.AddRequest;
import sk.glova.cryptowallet.domain.request.TransferRequest;
import sk.glova.cryptowallet.domain.request.UpsertWalletRequest;
import sk.glova.cryptowallet.services.WalletServiceImpl;

//TODO add swagger
@RestController
@RequestMapping(value = "rest/v1/wallet", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class WalletController {

    final private WalletServiceImpl service;

    @ResponseStatus(OK)
    @GetMapping("/getCurrencyRates")
    public Page<CryptoCurrencyRate> getAllCryptoCurrencies(Pageable pageable) {
        return service.getAllCryptoCurrencies(pageable);
    }

    @ResponseStatus(CREATED)
    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody UpsertWalletRequest request) {
        Wallet savedWallet = service.createWallet(request);
        return ResponseEntity.created(getCurrentLocationWithId(savedWallet.getId())).body(savedWallet);
    }

    @ResponseStatus(OK)
    @PutMapping("/{walletId}")
    public void updateWallet(
        @PathVariable Long walletId,
        @RequestBody UpsertWalletRequest request
    ) {
        service.updateWallet(walletId, request);
    }

    @ResponseStatus(OK)
    @GetMapping("/{walletId}")
    public Wallet getWallet(@PathVariable Long walletId) {
        return service.getWallet(walletId);
    }

    @ResponseStatus(OK)
    @DeleteMapping("/{walletId}")
    public void deleteWallet(@PathVariable Long walletId) {
        service.deleteWallet(walletId);
    }

    @ResponseStatus(OK)
    @PostMapping("/{walletId}/add")
    public void add(
        @PathVariable Long walletId,
        @RequestBody AddRequest addRequest
    ) {
        service.add(walletId, addRequest);
    }

    @ResponseStatus(OK)
    @PostMapping("/{walletId}/transfer")
    public void transfer(
        @PathVariable Long walletId,
        @RequestBody TransferRequest transferRequest
    ) {
        service.transfer(walletId, transferRequest);
    }

}
