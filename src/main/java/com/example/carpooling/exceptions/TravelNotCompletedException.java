package com.example.carpooling.exceptions;

public class TravelNotCompletedException extends  RuntimeException{
    public TravelNotCompletedException(String message) {
        super(message);
    }
}
