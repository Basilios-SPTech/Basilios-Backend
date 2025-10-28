package com.basilios.basilios.core.exception;

public class InvalidDistanceException extends RuntimeException {
    private final double distance;

    public InvalidDistanceException(String message, double distance) {
        super(message);
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }
}