package com.basilios.basilios.core.exception;

public class ProdutoNotFoundException extends RuntimeException {
    private final Long productId;

    public ProdutoNotFoundException(Long productId) {
        super("Produto não encontrado com ID: " + productId);
        this.productId = productId;
    }

    public ProdutoNotFoundException(String message, Long productId) {
        super(message);
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}