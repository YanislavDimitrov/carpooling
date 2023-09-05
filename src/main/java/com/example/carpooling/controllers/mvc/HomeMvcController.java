package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.models.Image;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.models.enums.UserStatus;
import com.example.carpooling.services.contracts.TravelService;
import com.example.carpooling.services.contracts.UserService;
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
    private final UserService userService;

    public HomeMvcController(AuthenticationHelper authenticationHelper, TravelService travelService, UserService userService) {
        this.authenticationHelper = authenticationHelper;
        this.travelService = travelService;
        this.userService = userService;
    }

    @GetMapping
    public String viewHomePage(Model model) {
        model.addAttribute("completedTravels", travelService.countCompleted());
        model.addAttribute("createdUsers", userService.count());
        model.addAttribute("topDrivers", userService.findTopTenDrivers());
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

    @ModelAttribute("hasProfilePicture")
    public Boolean hasProfilePicture(HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            return loggedUser.getProfilePicture() != null;
        } catch (AuthenticationFailureException e) {
            return false;
        }
    }

    @ModelAttribute("profilePicture")
    public Image populateProfilePicture(HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            return loggedUser.getProfilePicture();
        } catch (AuthenticationFailureException e) {
            return null;
        }
    }

    @ModelAttribute("isBlocked")
    public boolean populateIsActive(HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            return loggedUser.getStatus() == UserStatus.BLOCKED;
        } catch (AuthenticationFailureException e) {
            return false;
        }
    }

    @ModelAttribute("latestTravels")
    public List<Travel> populateMostPopularTravels() {
        return travelService.findLatestTravels();
    }

    @ModelAttribute("travels")
    public List<Travel> populatePlannedTravels() {
        return travelService.findAllByStatusPlanned();
    }
}
