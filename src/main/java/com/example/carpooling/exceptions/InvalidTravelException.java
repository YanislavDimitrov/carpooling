package com.example.carpooling.exceptions;

public class InvalidTravelException extends RuntimeException {
    public InvalidTravelException(String message) {
        super(message);
    }
}
