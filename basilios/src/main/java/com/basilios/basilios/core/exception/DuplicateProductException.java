package com.basilios.basilios.core.exception;

public class DuplicateProductException extends RuntimeException {
    private final String existingProductName;

    public DuplicateProductException(String existingProductName) {
        super("Já existe um produto com o nome: " + existingProductName);
        this.existingProductName = existingProductName;
    }

    public DuplicateProductException(String message, String existingProductName) {
        super(message);
        this.existingProductName = existingProductName;
    }

    public String getExistingProductName() {
        return existingProductName;
    }
}