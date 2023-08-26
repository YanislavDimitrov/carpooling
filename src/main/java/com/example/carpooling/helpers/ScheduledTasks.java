package com.example.carpooling.helpers;

import com.example.carpooling.repositories.contracts.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
    private final TokenRepository tokenRepository;

    @Autowired
    public ScheduledTasks(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void clearExpiredTokes() {
        this.tokenRepository.findAll()
                .forEach(token -> {
                    if (token.isExpired()) {
                        this.tokenRepository.delete(token);
                    }
                });
    }
}
