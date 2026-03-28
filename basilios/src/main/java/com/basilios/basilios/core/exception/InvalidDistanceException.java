package com.basilios.basilios.core.exception;

import lombok.Getter;

@Getter
public class InvalidDistanceException extends RuntimeException {
    private final double distance;

    public InvalidDistanceException(String message, double distance) {
        super(message);
        this.distance = distance;
    }
}