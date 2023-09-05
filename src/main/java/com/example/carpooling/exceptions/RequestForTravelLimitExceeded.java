package com.example.carpooling.exceptions;

public class RequestForTravelLimitExceeded extends RuntimeException {
    public RequestForTravelLimitExceeded(String message) {
        super(message);
    }
}
