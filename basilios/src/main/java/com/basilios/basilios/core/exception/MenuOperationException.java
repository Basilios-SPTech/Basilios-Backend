package com.basilios.basilios.core.exception;

public class MenuOperationException extends RuntimeException {
    private final String operation;
    private final Long productId;

    public MenuOperationException(String message, String operation, Long productId) {
        super(message);
        this.operation = operation;
        this.productId = productId;
    }

    public static MenuOperationException cannotPause(Long productId, String reason) {
        return new MenuOperationException(
                "Não é possível pausar o produto ID " + productId + ": " + reason,
                "PAUSE",
                productId
        );
    }

    public static MenuOperationException cannotActivate(Long productId, String reason) {
        return new MenuOperationException(
                "Não é possível ativar o produto ID " + productId + ": " + reason,
                "ACTIVATE",
                productId
        );
    }

    public static MenuOperationException cannotDelete(Long productId, String reason) {
        return new MenuOperationException(
                "Não é possível deletar o produto ID " + productId + ": " + reason,
                "DELETE",
                productId
        );
    }

    public String getOperation() {
        return operation;
    }

    public Long getProductId() {
        return productId;
    }
}