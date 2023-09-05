package com.example.carpooling.services;

import com.example.carpooling.models.User;
import com.example.carpooling.models.VerificationToken;
import com.example.carpooling.repositories.contracts.TokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;

import static com.example.carpooling.Helpers.createMockUser;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ValidationServiceImplTests {
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private JavaMailSender javaMailSender;
    private ValidationServiceImpl validationService;

    @BeforeEach
    public void setUp() {
        validationService = new ValidationServiceImpl(new MockEnvironment(), tokenRepository, javaMailSender);
    }

    @Test
    public void validate_Should_Invoke_Save() throws MessagingException, IOException {
        //Assert
        User mockUser = createMockUser();
        VerificationToken verificationToken = new VerificationToken(mockUser);
        Mockito.when(javaMailSender.createMimeMessage()).thenReturn(Mockito.mock(MimeMessage.class));

        //Act
        validationService.validate(mockUser);

        //Assert
        Mockito.verify(tokenRepository, Mockito.times(1)).save(any(VerificationToken.class));
        Mockito.verify(javaMailSender, Mockito.times(1)).send(any(MimeMessage.class));
    }
}
