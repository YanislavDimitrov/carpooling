package com.example.carpooling.exceptions;

public class VehicleIsFullException extends RuntimeException {
    public VehicleIsFullException(String message) {
        super(message);
    }
}
