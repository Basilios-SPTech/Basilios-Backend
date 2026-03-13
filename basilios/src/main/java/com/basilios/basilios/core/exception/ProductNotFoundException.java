package com.basilios.basilios.core.exception;

import lombok.Getter;

@Getter
public class ProductNotFoundException extends RuntimeException {
    private final Long productId;

    public ProductNotFoundException(Long productId) {
        super("Produto não encontrado com ID: " + productId);
        this.productId = productId;
    }

    public ProductNotFoundException(String message, Long productId) {
        super(message);
        this.productId = productId;
    }
}