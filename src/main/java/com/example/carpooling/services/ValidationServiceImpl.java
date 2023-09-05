package com.example.carpooling.services;

import com.example.carpooling.models.User;
import com.example.carpooling.models.VerificationToken;
import com.example.carpooling.repositories.contracts.TokenRepository;
import com.example.carpooling.services.contracts.BingMapsService;
import com.example.carpooling.services.contracts.ValidationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.example.carpooling.services.UserServiceImpl.CONFIRMATION_EMAIL_TEMPLATE_PATH;

@Service
public class ValidationServiceImpl implements ValidationService {
    private final TokenRepository tokenRepository;
    private final JavaMailSender javaMailSender;
    private final BingMapsService bingMapsService;
    @Value("env.basepath")
    private String baseUrl;

    @Autowired
    public ValidationServiceImpl(TokenRepository tokenRepository, JavaMailSender javaMailSender, BingMapsService bingMapsService) {
        this.tokenRepository = tokenRepository;
        this.javaMailSender = javaMailSender;
        this.bingMapsService = bingMapsService;
    }

    @Override
    public void validate(User user) throws IOException, MessagingException {
        VerificationToken verificationToken = new VerificationToken(user);
        tokenRepository.save(verificationToken);
        String verificationLink = baseUrl + "/verification/validate?token=" + verificationToken.getToken();
        String htmlContent = readHtmlFromFile();
        htmlContent = htmlContent.replace("verificationLinkPlaceholder", verificationLink);
        sendVerificationEmail(user.getEmail(), htmlContent);
    }

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
