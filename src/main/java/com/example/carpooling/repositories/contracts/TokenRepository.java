package com.example.carpooling.repositories.contracts;

import com.example.carpooling.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);

    void deleteAllByUserId(Long userId);
}
