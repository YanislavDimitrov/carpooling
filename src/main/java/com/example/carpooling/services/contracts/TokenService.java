package com.example.carpooling.services.contracts;

import com.example.carpooling.models.VerificationToken;

public interface TokenService {
    void deleteAllByUserId(Long id);

    VerificationToken findByToken(String token);
}
