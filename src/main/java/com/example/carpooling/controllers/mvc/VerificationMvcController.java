package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.models.User;
import com.example.carpooling.models.VerificationToken;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.services.contracts.TokenService;
import com.example.carpooling.services.contracts.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/verification")
public class VerificationMvcController {
    private final TokenService tokenService;
    private final UserService userService;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public VerificationMvcController(TokenService tokenService, UserService userService, AuthenticationHelper authenticationHelper) {
        this.tokenService = tokenService;
        this.userService = userService;
        this.authenticationHelper = authenticationHelper;
    }

    @ModelAttribute("isAuthenticated")
    public boolean populateIsAuthenticated(HttpSession session) {
        return session.getAttribute("currentUser") != null;
    }

    @ModelAttribute("isAdmin")
    public boolean populateIsAdmin(HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            return loggedUser.getRole() == UserRole.ADMIN;
        } catch (AuthenticationFailureException e) {
            return false;
        }
    }

    @GetMapping("validate")
    public String verifyEmail(@RequestParam("token") String token) {
        VerificationToken verificationToken = tokenService.findByToken(token);
        if (verificationToken != null && !verificationToken.isExpired()) {
            this.userService.verify(verificationToken.getUser().getId());
            this.tokenService.deleteAllByUserId(verificationToken.getUser().getId());
            return "VerificationSuccessView";
        } else {
            return "Invalid or expired token.";
        }
    }
}
