package com.example.carpooling.controllers.rest;

import com.example.carpooling.models.VerificationToken;
import com.example.carpooling.repositories.contracts.TokenRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationController {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Autowired
    public VerificationController(TokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken != null && !verificationToken.isExpired()) {
            this.userRepository.validate(verificationToken.getUser().getId());
            return ResponseEntity.ok("Email verified successfully!");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }
    }
}
