package com.basilios.basilios.core.exception;

import java.util.List;

public class ProductUnavailableException extends RuntimeException {
    private final List<String> unavailableProducts;

    public ProductUnavailableException(String message, List<String> unavailableProducts) {
        super(message);
        this.unavailableProducts = unavailableProducts;
    }

    public ProductUnavailableException(List<String> unavailableProducts) {
        super("Os seguintes produtos não estão disponíveis: " + String.join(", ", unavailableProducts));
        this.unavailableProducts = unavailableProducts;
    }

    public List<String> getUnavailableProducts() {
        return unavailableProducts;
    }
}