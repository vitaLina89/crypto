package org.example.crypto.initializer;

import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crypto.model.Crypto;
import org.example.crypto.service.CryptoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class DbInitializer implements CommandLineRunner {

    private final CryptoService cryptoService;
    private final ConnectionFactory connectionFactory;

    @Override
    public void run(String... args) {
        log.info("Initializing database ...");

        createTableIfNotExists()
                .thenMany(Flux.fromIterable(CRYPTOS))  // передаем список криптовалют
                .flatMap(this::saveCrypto)              // передаем каждую криптовалюту в метод saveCrypto
                .doOnComplete(() -> log.info("Processing complete."))
                .doOnError(ex -> log.error("Unable to save crypto!", ex))
                .subscribe();
    }

    // Метод, который обрабатывает каждую криптовалюту
    public Mono<Crypto> saveCrypto(Crypto crypto) {
        String sql = """
            MERGE INTO cryptos (symbol, price, likes)
            KEY (symbol)
            VALUES ($1, $2, $3);
        """;
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, crypto.getSymbol())
                        .bind(1, crypto.getPrice())
                        .bind(2, crypto.getLikes())
                        .execute()))
                .doOnSuccess(result -> log.info("Crypto {} saved or updated", crypto.getSymbol()))
                .doOnError(ex -> log.error("Error saving or updating crypto {}", crypto.getSymbol(), ex))
                .thenReturn(crypto);
    }

    private Mono<Void> createTableIfNotExists() {
        String sql = """
        CREATE TABLE IF NOT EXISTS cryptos (
            symbol VARCHAR(255) PRIMARY KEY,
            price DECIMAL(10,2) NOT NULL,
            likes INT
        );
    """;

        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.createStatement(sql).execute()))
                .doOnSuccess(result -> log.info("Table 'cryptos' is ready"))
                .doOnError(ex -> log.error("Error creating table 'cryptos'", ex))
                .then();
    }

    // Список криптовалют
    private static final List<Crypto> CRYPTOS = List.of(
            new Crypto("BTC", BigDecimal.valueOf(20000), 0),
            new Crypto("ETH", BigDecimal.valueOf(10000), 0)
    );
}
