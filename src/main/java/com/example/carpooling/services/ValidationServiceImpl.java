package com.example.carpooling.services;

import com.example.carpooling.models.User;
import com.example.carpooling.models.VerificationToken;
import com.example.carpooling.repositories.contracts.TokenRepository;
import com.example.carpooling.services.contracts.ValidationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * The {@code ValidationServiceImpl} class provides methods for user account validation,
 * including sending verification emails and generating verification tokens.
 * It interacts with the TokenRepository to manage user verification tokens
 * and uses JavaMailSender for sending verification emails.
 *
 * @author Yanislav Dimitrov & Ivan Boev
 * @version 1.0
 * @since 06.09.23
 */
@Service
@PropertySource("classpath:application.properties")
public class ValidationServiceImpl implements ValidationService {
    private final TokenRepository tokenRepository;
    private final JavaMailSender javaMailSender;
    private String baseUrl;

    /**
     * Constructs a new {@code ValidationServiceImpl} with the specified dependencies.
     *
     * @param env             The environment containing configuration properties.
     * @param tokenRepository The repository for managing verification tokens.
     * @param javaMailSender  The JavaMailSender for sending email messages.
     */
    @Autowired
    public ValidationServiceImpl(Environment env, TokenRepository tokenRepository, JavaMailSender javaMailSender) {
        this.tokenRepository = tokenRepository;
        this.javaMailSender = javaMailSender;
        this.baseUrl = env.getProperty("env.basepath");
    }

    /**
     * Validates a user's account by generating a verification token, saving it in the repository,
     * and sending a verification email containing a verification link.
     *
     * @param user The user whose account is being validated.
     * @throws IOException        If an error occurs while reading the HTML email template.
     * @throws MessagingException If an error occurs while sending the verification email.
     */
    @Override
    public void validate(User user) throws IOException, MessagingException {
        VerificationToken verificationToken = new VerificationToken(user);
        tokenRepository.save(verificationToken);
        String verificationLink = baseUrl + "verification/validate?token=" + verificationToken.getToken();
        String htmlContent = readHtmlFromFile();
        htmlContent = htmlContent.replace("verificationLinkPlaceholder", verificationLink);
        sendVerificationEmail(user.getEmail(), htmlContent);
    }

    /**
     * Reads the HTML content of the email template from a file.
     *
     * @return The HTML content of the email template as a string.
     * @throws IOException If an error occurs while reading the file.
     */
    private static String readHtmlFromFile() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/WelcomeTemplateEmail.html");
        InputStream inputStream = resource.getInputStream();

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    /**
     * Sends a verification email to the specified email address with the provided content.
     *
     * @param emailAddress The recipient's email address.
     * @param content      The HTML content of the email.
     * @throws MessagingException If an error occurs while sending the email.
     */
    private void sendVerificationEmail(String emailAddress, String content) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        ClassPathResource imageResource = new ClassPathResource("static/images/CoverPicture.svg");
        helper.addInline("image123", imageResource);

        helper.setFrom("carpoolingalpha@gmail.com");
        helper.setTo(emailAddress);
        helper.setSubject("Carpool Email Verification");
        helper.setText(content, true);

        javaMailSender.send(message);
    }


}
