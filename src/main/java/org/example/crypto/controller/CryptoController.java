package org.example.crypto.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crypto.service.CryptoService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Controller
public class CryptoController {

    private final CryptoService cryptoService;

    //-- REQUEST_RESPONSE
    @MessageMapping("get.{symbol}.price")
    public Mono<BigDecimal> getCryptoPrice(@DestinationVariable String symbol) {
        log.info("Get {} price", symbol);
        return cryptoService.getPrice(symbol);
    }

    //-- REQUEST_STREAM
    @MessageMapping("stream.{symbol}.price")
    public Flux<BigDecimal> streamCryptoPrice(@DestinationVariable String symbol,
                                              @Payload(required = false) PriceStreamRequest request) {
        int actualInterval = (request != null && request.intervalInSeconds() > 0) ?
                request.intervalInSeconds() : DEFAULT_INTERVAL_SECONDS;
        return cryptoService.streamPrice(symbol, actualInterval)
                .doFirst(() -> log.info("Start streaming {} with interval {}", symbol, actualInterval))
                .doOnCancel(() -> log.info("Finish streaming {} with interval {}", symbol, actualInterval));
    }

    //-- FIRE_AND_FORGET
    @MessageMapping("like.{symbol}")
    public Mono<Void> likeCrypto(@DestinationVariable String symbol) {
        log.info("Like {}", symbol);
        return cryptoService.like(symbol);
    }

    //-- REQUEST_RESPONSE
    @MessageMapping("get.{symbol}.likes")
    public Mono<Integer> getCryptoLikes(@DestinationVariable String symbol) {
        log.info("Get {} likes", symbol);
        return cryptoService.getLikes(symbol);
    }

    @MessageExceptionHandler(Exception.class)
    public Mono<Exception> exceptionHandler(Exception exception) {
        log.error(exception.getMessage());
        return Mono.error(exception);
    }

    private static final int DEFAULT_INTERVAL_SECONDS = 5;
}