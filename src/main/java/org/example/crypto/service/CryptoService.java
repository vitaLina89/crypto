package org.example.crypto.service;


import lombok.RequiredArgsConstructor;
import org.example.crypto.exception.CryptoNotFoundException;
import org.example.crypto.model.Crypto;
import org.example.crypto.repository.CryptoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

@RequiredArgsConstructor
@Service
public class CryptoService {

    private final CryptoRepository cryptoRepository;

    public Mono<Crypto> validateAndGetCrypto(String symbol) {
        return cryptoRepository.findById(symbol.toUpperCase())
                .switchIfEmpty(Mono.error(() -> new CryptoNotFoundException(symbol)));
    }

    public Mono<BigDecimal> getPrice(String symbol) {
        return validateAndGetCrypto(symbol).map(Crypto::getPrice);
    }

    public Flux<BigDecimal> streamPrice(String symbol, int interval) {
        return Flux.interval(Duration.ofSeconds(interval)).flatMap(tick -> getPrice(symbol));
    }

    public Mono<Void> like(String symbol) {
        return validateAndGetCrypto(symbol)
                .flatMap(crypto -> {
                    crypto.setLikes(crypto.getLikes() + 1);
                    return saveCrypto(crypto);
                })
                .then();
    }

    public Mono<Integer> getLikes(String symbol) {
        return validateAndGetCrypto(symbol).map(Crypto::getLikes);
    }

    public Mono<Crypto> saveCrypto(Crypto crypto) {
        return cryptoRepository.save(crypto);
    }

    public Flux<Crypto> getCryptos() {
        return cryptoRepository.findAll();
    }
}
