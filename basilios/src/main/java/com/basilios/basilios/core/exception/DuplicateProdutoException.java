package com.basilios.basilios.core.exception;

public class DuplicateProdutoException extends RuntimeException {
    private final String existingProductName;

    public DuplicateProdutoException(String existingProductName) {
        super("Já existe um produto com o nome: " + existingProductName);
        this.existingProductName = existingProductName;
    }

    public DuplicateProdutoException(String message, String existingProductName) {
        super(message);
        this.existingProductName = existingProductName;
    }

    public String getExistingProductName() {
        return existingProductName;
    }
}