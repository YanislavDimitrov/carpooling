package com.example.carpooling.services.contracts;

import com.example.carpooling.models.User;
import jakarta.mail.MessagingException;

import java.io.IOException;

public interface ValidationService {
    void validate(User user) throws IOException, MessagingException;
}
