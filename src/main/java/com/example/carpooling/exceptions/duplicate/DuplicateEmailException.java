package com.example.carpooling.exceptions.duplicate;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String type, String value) {
        super(String.format("%s with email %s already exists.", type, value));
    }

    public DuplicateEmailException(String message) {
        super(message);
    }
}
