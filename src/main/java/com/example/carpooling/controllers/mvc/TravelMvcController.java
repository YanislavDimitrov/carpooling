package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.exceptions.InvalidOperationException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.helpers.mappers.TravelMapper;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.TravelCreationOrUpdateDto;
import com.example.carpooling.models.dtos.TravelFrontEndView;
import com.example.carpooling.models.dtos.TravelViewDto;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.services.contracts.TravelService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/travels")
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

        List<TravelFrontEndView> travels = travelService.findAllByStatusPlanned()
                .stream()
                .map(travelMapper::fromTravelToFrontEnd)
                .toList();


        model.addAttribute("travels", travels);
        return "TravelsView";
    }

    @GetMapping("/user")
    public String viewAllTravelsForUser(HttpSession session, Model model) {
        User user;
        try {
            user = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        List<TravelFrontEndView> travels = new ArrayList<>(travelService.findTravelByUser(user)
                .stream()
                .map(travelMapper::fromTravelToFrontEnd)
                .toList());

        List<TravelFrontEndView> travelsAsPassenger = travelService.findTravelsAsPassengerByUser(user)
                .stream()
                .map(travelMapper::fromTravelRequest)
                .map(travelMapper::fromTravelToFrontEnd)
                .toList();
//        travels.addAll(travelsAsPassenger);
        model.addAttribute("travels",travels);
        model.addAttribute("travelsAsPassenger",travelsAsPassenger);
        //ToDo View
        return "UserTravelsView";
    }
    @GetMapping("/latest")
    public String getLatestTravels(Model model) {
        List<Travel> latestTravels = travelService.findLatestTravels();
        model.addAttribute("latestTravels",latestTravels);
        return "";
    }

    @GetMapping("/completed")
    public String viewCompletedTravels(Model model, HttpSession session) {
        try {
            authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
        List<TravelFrontEndView> travels = travelService.getAllCompleted()
                .stream()
                .map(travelMapper::fromTravelToFrontEnd)
                .toList();
        model.addAttribute("travels",travels);
        return "CompletedTravelsView";
    }

    @GetMapping("/{id}")
    public String viewTravel(@PathVariable Long id, Model model, HttpSession session) {
        try {
            authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
        TravelFrontEndView travelFrontEndView = travelMapper.fromTravelToFrontEnd(travelService.getById(id));
        model.addAttribute("travel", travelFrontEndView);
        return "TravelView";
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
    public String createTravel(@Valid @ModelAttribute("travel") TravelCreationOrUpdateDto travel,
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
        try {
            travelService.create(newTravel, driver);
        } catch (InvalidOperationException e) {
            errors.rejectValue("departureTime", "creation_error", e.getMessage());
            return "NewTravelView";
        }
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
        model.addAttribute("travelId",id);

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
        Travel travel = travelService.getById(id);
        Travel travelUpdate = travelMapper.toTravelFromTravelUpdateSaveDto(travel,travelUpdateDto);
        if (errors.hasErrors()) {
            return "UpdateTravelView";
        }

        try {
            travelService.update(travelUpdate, loggedUser);
        } catch (AuthorizationException | InvalidOperationException e) {
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
            travelService.delete(id, loggedUser);
        } catch (EntityNotFoundException | AuthorizationException e) {
            return "AccessDeniedView";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
        return "redirect:/travels";
    }
    @GetMapping("/{id}/cancel")
    public String cancelTravel( @PathVariable Long id , HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            Travel travel = travelService.getById(id);
            travelService.cancelTravel(id,loggedUser);
            return "UserTravelsView";
        } catch (AuthenticationFailureException | AuthorizationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        } catch (InvalidOperationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
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

    @ModelAttribute("isAuthenticated")
    public boolean populateIsAuthenticated(HttpSession session) {
        return session.getAttribute("currentUser") != null;
    }
}
