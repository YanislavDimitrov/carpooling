package com.example.carpooling.services;

import com.example.carpooling.models.VerificationToken;
import com.example.carpooling.repositories.contracts.TokenRepository;
import com.example.carpooling.services.contracts.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;

    @Autowired
    public TokenServiceImpl(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    @Transactional
    public void deleteAllByUserId(Long id) {
        this.tokenRepository.deleteAllByUserId(id);
    }

    @Override
    public VerificationToken findByToken(String token) {
        return this.tokenRepository.findByToken(token);
    }
}
