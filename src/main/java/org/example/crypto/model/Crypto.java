package org.example.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cryptos")
public class Crypto {

    @Id
    private String symbol;
    @Column(value = "price")
    private BigDecimal price;
    @Column(value = "likes")
    private Integer likes;
}
