package com.basilios.basilios.core.exception;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InvalidPriceException extends RuntimeException {
    private final BigDecimal providedPrice;
    private final BigDecimal minAllowedPrice;

    public InvalidPriceException(String message, BigDecimal providedPrice, BigDecimal minAllowedPrice) {
        super(message);
        this.providedPrice = providedPrice;
        this.minAllowedPrice = minAllowedPrice;
    }

    public InvalidPriceException(BigDecimal providedPrice) {
        super("Preço inválido: " + providedPrice + ". O preço deve ser maior que zero.");
        this.providedPrice = providedPrice;
        this.minAllowedPrice = BigDecimal.ZERO;
    }
}
