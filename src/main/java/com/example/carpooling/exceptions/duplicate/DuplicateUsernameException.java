package com.example.carpooling.exceptions.duplicate;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String type, String value) {
        super(String.format("%s with username %s already exists.", type, value));
    }

    public DuplicateUsernameException(String message) {
        super(message);
    }
}
