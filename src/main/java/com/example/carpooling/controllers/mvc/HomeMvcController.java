package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.services.contracts.TravelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/")
public class HomeMvcController {
    private final AuthenticationHelper authenticationHelper;
    private final TravelService travelService;

    public HomeMvcController(AuthenticationHelper authenticationHelper, TravelService travelService) {
        this.authenticationHelper = authenticationHelper;
        this.travelService = travelService;
    }

    @GetMapping
    public String viewHomePage(Model model) {
        long completedTravels = travelService.countCompleted();
        model.addAttribute("completedTravels",completedTravels);
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
    @ModelAttribute("popularTravels")
    public List<Travel> populateMostPopularTravels() {
        return travelService.getTopRatedTravels();
    }
}
