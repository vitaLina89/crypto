package org.example.crypto.simulator;


import lombok.RequiredArgsConstructor;
import org.example.crypto.model.Crypto;
import org.example.crypto.service.CryptoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Random;

@ConditionalOnProperty(value = "simulator.enabled", havingValue = "true")
@RequiredArgsConstructor
@EnableScheduling
@Component
public class CryptoSimulator {

    private final CryptoService cryptoService;

    @Scheduled(fixedRate = 1000)
    private Mono<Void> simulate() {
        return cryptoService.getCryptos()
                .filter(crypto -> hasTrade())
                .flatMap(this::updateCryptoPrice)
                .then();
    }

    private boolean hasTrade() {
        return RAND.nextBoolean();
    }

    private Mono<Crypto> updateCryptoPrice(Crypto crypto) {
        BigDecimal newPrice = calculateNewPrice(crypto.getPrice());
        crypto.setPrice(newPrice);
        return cryptoService.saveCrypto(crypto);
    }

    private BigDecimal calculateNewPrice(BigDecimal currentValue) {
        BigDecimal change = BigDecimal.valueOf(RAND.nextDouble() * 100);
        boolean sign = RAND.nextBoolean();
        BigDecimal variation = sign ? change : change.negate();
        return currentValue.add(variation).setScale(2, RoundingMode.HALF_UP);
    }

    private static final Random RAND = new SecureRandom();
}
