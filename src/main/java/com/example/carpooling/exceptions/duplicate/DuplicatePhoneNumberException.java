package com.example.carpooling.exceptions.duplicate;

public class DuplicatePhoneNumberException extends RuntimeException {
    public DuplicatePhoneNumberException(String type, String value) {
        super(String.format("%s with phone number %s already exists.", type, value));
    }

    public DuplicatePhoneNumberException(String message) {
        super(message);
    }
}
