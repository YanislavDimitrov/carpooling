package com.example.carpooling.services;

import com.example.carpooling.models.VerificationToken;
import com.example.carpooling.repositories.contracts.TokenRepository;
import com.example.carpooling.services.contracts.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The {@code TokenServiceImpl} class is responsible for managing and interacting with verification tokens in the carpooling application.
 * It provides methods to delete all tokens associated with a user by their ID and to find a verification token by its token string.
 *
 * <p>This class is annotated with {@code @Service} to indicate that it is a Spring service component, making it eligible
 * for automatic dependency injection.</p>
 *
 * @author Yanislav Dimitrov & Ivan Boev
 * @version 1.0
 * @since 06.09.23
 */
@Service
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;

    /**
     * Constructs a {@code TokenServiceImpl} instance with the necessary dependencies.
     *
     * @param tokenRepository The repository for storing and managing verification tokens.
     */
    @Autowired
    public TokenServiceImpl(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Deletes all verification tokens associated with a user by their ID.
     *
     * @param id The unique identifier of the user for whom the tokens should be deleted.
     */
    @Override
    @Transactional
    public void deleteAllByUserId(Long id) {
        this.tokenRepository.deleteAllByUserId(id);
    }

    /**
     * Finds a verification token by its token string.
     *
     * @param token The token string used to identify and retrieve the verification token.
     * @return The {@code VerificationToken} entity associated with the provided token string, or {@code null} if not found.
     */
    @Override
    public VerificationToken findByToken(String token) {
        return this.tokenRepository.findByToken(token);
    }
}
