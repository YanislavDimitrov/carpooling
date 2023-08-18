package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.services.contracts.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeMvcController {
    private final AuthenticationHelper authenticationHelper;

    public HomeMvcController(AuthenticationHelper authenticationHelper) {
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping
    public String viewHomePage() {
        return "index";
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
}
