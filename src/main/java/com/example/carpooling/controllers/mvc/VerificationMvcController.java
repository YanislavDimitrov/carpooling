package com.example.carpooling.controllers.mvc;

import com.example.carpooling.models.VerificationToken;
import com.example.carpooling.repositories.contracts.TokenRepository;
import com.example.carpooling.services.contracts.TokenService;
import com.example.carpooling.services.contracts.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/verification")
public class VerificationMvcController {
    private final TokenService tokenService;
    private final UserService userService;

    @Autowired
    public VerificationMvcController(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @GetMapping("verify")
    public String verifyEmail(@RequestParam("token") String token) {
        VerificationToken verificationToken = tokenService.findByToken(token);
        if (verificationToken != null && !verificationToken.isExpired()) {
            this.userService.verify(verificationToken.getUser().getId());
            this.tokenService.deleteAllByUserId(verificationToken.getUser().getId());
            return "Email verified successfully МАДАФАКА!";
        } else {
            return "Invalid or expired token.";
        }
    }
}
