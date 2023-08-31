package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.*;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.helpers.ExtractionHelper;
import com.example.carpooling.helpers.mappers.FeedbackMapper;
import com.example.carpooling.models.Feedback;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.FeedbackFilterDto;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.services.contracts.FeedbackService;
import com.example.carpooling.services.contracts.TravelService;
import com.example.carpooling.services.contracts.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/feedbacks")
public class FeedbackMvcController {

    private final FeedbackService feedbackService;
    private final FeedbackMapper feedbackMapper;
    private final AuthenticationHelper authenticationHelper;
    private final UserService userService;
    private final TravelService travelService;
    private final ExtractionHelper extractionHelper;

    @Autowired
    public FeedbackMvcController(FeedbackService feedbackService, FeedbackMapper feedbackMapper, AuthenticationHelper authenticationHelper, UserService userService, TravelService travelService, ExtractionHelper extractionHelper) {
        this.feedbackService = feedbackService;
        this.feedbackMapper = feedbackMapper;
        this.authenticationHelper = authenticationHelper;
        this.userService = userService;
        this.travelService = travelService;
        this.extractionHelper = extractionHelper;
    }


    @GetMapping
    public String get(HttpSession session,
                      Model model,
                      @ModelAttribute("filter") FeedbackFilterDto filter,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "5") int size,
                      HttpServletRequest request) {
        try {
            authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
        List<Feedback> feedbacks = feedbackService.get();

        Sort sort;
        if (filter.getSortOrder().equalsIgnoreCase("desc")) {
            sort = Sort.by(Sort.Direction.DESC, filter.getSortBy());
        } else {
            sort = Sort.by(Sort.Direction.ASC, filter.getSortBy());
        }
        Page<Feedback> paginatedFeedbacks = feedbackService.findAllPaginated(
                page,
                size,
                sort,
                filter.getRating(),
                filter.getCreator(),
                filter.getRecipient(),
                filter.getTravel());

        Map<String, String[]> parameterMap = request.getParameterMap();
        String parameters = extractionHelper.extractParametersSection(parameterMap);

        model.addAttribute("filter", filter);
        model.addAttribute("feedbackPage", paginatedFeedbacks);
        model.addAttribute("filterParams", parameters);
        model.addAttribute("feedbacks", feedbacks);
        return "FeedbacksView";

    }

    @GetMapping("/travel/{id}")
    public String getFeedbacksByTravelId(@PathVariable Long id, HttpSession session, Model model) {
        try {
            authenticationHelper.tryGetUser(session);
            List<Feedback> feedbacks = feedbackService.findByTravelId(id);
            return "redirect:/travels/" + id;
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }
    }

    @GetMapping("/user")
    public String getFeedbacksByUser( HttpSession session , Model model) {
     User recipient;
        try {
           recipient =   authenticationHelper.tryGetUser(session);
           List<Feedback> feedbacks = feedbackService.getByRecipientIs(recipient);
           model.addAttribute("feedbacks",feedbacks);
           return "UserFeedbacksView";
        }catch (AuthenticationFailureException e ) {
            return "redirect:/auth/login";
        }catch (EntityNotFoundException e) {
            return "NotFoundView";
        }

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
    @ModelAttribute("travels")
    public List<Travel> populateTravels() {
        return travelService.get();
    }

    @ModelAttribute("users")
    public List<User> populateUsers() {
        return userService.getAll();
    }

    @ModelAttribute("isAuthenticated")
    public boolean populateIsAuthenticated(HttpSession session) {
        return session.getAttribute("currentUser") != null;
    }
}
