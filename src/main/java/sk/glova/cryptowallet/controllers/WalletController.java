package sk.glova.cryptowallet.controllers;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static sk.glova.cryptowallet.utils.ControllerUtils.getCurrentLocationWithId;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import sk.glova.cryptowallet.domain.model.Wallet;
import sk.glova.cryptowallet.domain.request.AddRequest;
import sk.glova.cryptowallet.domain.request.TransferRequest;
import sk.glova.cryptowallet.domain.request.UpsertWalletRequest;
import sk.glova.cryptowallet.services.impl.WalletServiceImpl;

// TODO add endpoint for withdraws crypto currency from wallet into fiat currency (opposite of add endpoint)
@RestController
@RequestMapping(value = "rest/v1/wallet", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class WalletController {

    final private WalletServiceImpl service;

    @Operation(description = "Creates an empty wallet with the name based on upsert request DTO.")
    @ApiResponse(responseCode = "422", description = "When:<ul>" +
        "<li>provided name is already in use</li>" +
        "<li>provided name is null or empty string</li></ul>")
    @ResponseStatus(CREATED)
    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody UpsertWalletRequest request) {
        Wallet savedWallet = service.createWallet(request);
        return ResponseEntity.created(getCurrentLocationWithId(savedWallet.getId())).body(savedWallet);
    }

    @Operation(description = "Updates the wallet with given ID. Values are taken from given upsert request DTO.")
    @ApiResponse(responseCode = "404", description = "When wallet with given ID does not exist.")
    @ApiResponse(responseCode = "422", description = "When:<ul>" +
        "<li>provided name is already in use</li>" +
        "<li>provided name is the same as it was</li>" +
        "<li>provided name is null or empty string</li></ul>")
    @ResponseStatus(OK)
    @PutMapping("/{walletId}")
    public Wallet updateWallet(
        @PathVariable Long walletId,
        @RequestBody UpsertWalletRequest request
    ) {
        service.updateWallet(walletId, request);
        return service.getWallet(walletId);
    }

    @Operation(description = "Returns the wallet with given ID.")
    @ApiResponse(responseCode = "404", description = "When wallet with given ID does not exist.")
    @ResponseStatus(OK)
    @GetMapping("/{walletId}")
    public Wallet getWallet(@PathVariable Long walletId) {
        return service.getWallet(walletId);
    }

    @Operation(description = "Returns all wallets. Result is paginated with sorting capabilities.")
    @ResponseStatus(OK)
    @GetMapping
    public Page<Wallet> getWallets(@Parameter(hidden = true) Pageable pageable) {
        return service.getWallets(pageable);
    }

    @Operation(description = "Deletes the wallet with given ID.")
    @ApiResponse(responseCode = "404", description = "When wallet with given ID does not exist.")
    @ResponseStatus(OK)
    @DeleteMapping("/{walletId}")
    public Wallet deleteWallet(@PathVariable Long walletId) {
        final Wallet wallet = service.getWallet(walletId);
        service.deleteWallet(walletId);
        return wallet;
    }

    @Operation(description = "Adds crypto currency to wallet with given ID. It converts fiat currency into crypto currency according current rate " +
        "(if crypto currency exist in wallet - given amount increments the previous one, if does not exist - it creates new one with given amount).")
    @ApiResponse(responseCode = "404", description = "When wallet with given ID does not exist.")
    @ApiResponse(responseCode = "422", description = "When currency or cryptocurrency is not supported.")
    @ResponseStatus(OK)
    @PostMapping("/{walletId}/add")
    public Wallet add(
        @PathVariable Long walletId,
        @RequestBody AddRequest addRequest
    ) {
        service.add(walletId, addRequest);
        return service.getWallet(walletId);
    }

    @Operation(description = "Transfers crypto currency from wallet with given ID. It converts from one crypto currency into another according " +
        "current rate. It decrements wallet from which payment is outgoing and increment wallet to which payment is incoming (if currency exist in" +
        " wallet - given amount increments the previous one, if does not exist - it creates new one with given amount).")
    @ApiResponse(responseCode = "404", description = "When wallet with given ID does not exist.")
    @ApiResponse(responseCode = "422", description = "When:<ul>" +
        "<li>currency or cryptocurrency is not supported</li>" +
        "<li>wallet does not have specified currency</li>" +
        "<li>wallet does not have enough amount in specified currency</li></ul>")
    @ResponseStatus(OK)
    @PostMapping("/{walletId}/transfer")
    public Wallet transfer(
        @PathVariable Long walletId,
        @RequestBody TransferRequest transferRequest
    ) {
        service.transfer(walletId, transferRequest);
        return service.getWallet(walletId);
    }

}
