package com.basilios.basilios.exception;

import java.util.List;

public class ProdutoUnavailableException extends RuntimeException {
    private final List<String> unavailableProducts;

    public ProdutoUnavailableException(String message, List<String> unavailableProducts) {
        super(message);
        this.unavailableProducts = unavailableProducts;
    }

    public ProdutoUnavailableException(List<String> unavailableProducts) {
        super("Os seguintes produtos não estão disponíveis: " + String.join(", ", unavailableProducts));
        this.unavailableProducts = unavailableProducts;
    }

    public List<String> getUnavailableProducts() {
        return unavailableProducts;
    }
}