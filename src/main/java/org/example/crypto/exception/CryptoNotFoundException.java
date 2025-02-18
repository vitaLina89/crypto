package org.example.crypto.exception;

public class CryptoNotFoundException extends RuntimeException {

    public CryptoNotFoundException(String symbol) {
        super("The crypto %s not found!".formatted(symbol));
    }
}
