package org.example.crypto.repository;

import org.example.crypto.model.Crypto;
import org.springframework.data.r2dbc.repository.R2dbcRepository;


public interface CryptoRepository extends R2dbcRepository<Crypto, String> {

}
