package com.basilios.basilios.exception;

public class MenuException extends RuntimeException {
    private final String errorCode;

    public MenuException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public MenuException(String message) {
        super(message);
        this.errorCode = "MENU_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}