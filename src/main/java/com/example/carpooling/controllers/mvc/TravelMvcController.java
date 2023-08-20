package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.helpers.mappers.TravelMapper;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.TravelCreationOrUpdateDto;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.services.contracts.TravelService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@Controller
@RequestMapping("/travels")
@Validated
public class TravelMvcController {
    private final TravelService travelService;
    private final TravelMapper travelMapper;
    private final AuthenticationHelper authenticationHelper;
    public TravelMvcController(TravelService travelService,
                                TravelMapper travelMapper,
                                AuthenticationHelper authenticationHelper) {
        this.travelService = travelService;
        this.travelMapper = travelMapper;
        this.authenticationHelper = authenticationHelper;
    }
    @GetMapping
    public String viewAllTravels(Model model, HttpSession session) {
        try {
            authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        List<Travel> travels = travelService.get();
        model.addAttribute("travels", travels);
        return "TravelsView";
    }
    @GetMapping("/new")
    public String showNewTravelPage(Model model, HttpSession session) {
        try {
            authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
        model.addAttribute("travel", new TravelCreationOrUpdateDto());
        //ToDo the view
        return "NewTravelView";
    }
    @PostMapping("/new")
    public String createTravel(@Valid @ModelAttribute("travel")TravelCreationOrUpdateDto travel,
                             BindingResult errors,
                             HttpSession session) {
        User driver;
        try {
            driver = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        if (errors.hasErrors()) {
            return "NewTravelView";
        }
        Travel newTravel = travelMapper.toTravelFromTravelCreationDto(travel);
        travelService.create(newTravel,driver);
        return "redirect:/travels";
    }
    @GetMapping("/{id}/update")
    public String showUpdateTravelPage(@PathVariable Long id, HttpSession session, Model model) {
      Travel travel;
        try {
            User loggedUser = this.authenticationHelper.tryGetUser(session);
            travel = travelService.getById(id);
            if (!loggedUser.getRole().equals(UserRole.ADMIN) && !travel.getDriver().equals(loggedUser)) {
                return "AccessDeniedView";
            }
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }
TravelCreationOrUpdateDto travelCreationOrUpdateDto = travelMapper.fromTravel(id);
        model.addAttribute("updateTravel", travelCreationOrUpdateDto);

        return "UpdateTravelView";
    }
    @PostMapping("/{id}/update")
    public String updateTravel(@Valid
                               @ModelAttribute("updateTravel") TravelCreationOrUpdateDto travelUpdateDto,
                               BindingResult errors,
                               @PathVariable Long id,
                               HttpSession session
    ) {
        User loggedUser = this.authenticationHelper.tryGetUser(session);
        Travel travel = travelMapper.toTravelFromTravelCreationDto(travelUpdateDto);
        if (errors.hasErrors()) {
            return "UpdateTravelView";
        }

        try {
            travelService.update(travel, loggedUser);
        } catch (AuthorizationException e) {
            return "AccessDeniedView";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }
        return String.format("redirect:/travels/%d", id);
    }
    @GetMapping("/{id}/delete")
    public String deleteTravel(@PathVariable Long id, HttpSession session) {
        try {
            User loggedUser = this.authenticationHelper.tryGetUser(session);
            travelService.delete(id,loggedUser);
        } catch (EntityNotFoundException | AuthorizationException e) {
            return "AccessDeniedView";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
        return "redirect:/posts";
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
