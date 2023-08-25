package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.*;
import com.example.carpooling.exceptions.duplicate.DuplicateEntityException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.helpers.mappers.TravelMapper;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.TravelCreationOrUpdateDto;
import com.example.carpooling.models.dtos.TravelFrontEndView;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.services.contracts.TravelRequestService;
import com.example.carpooling.services.contracts.TravelService;
import com.example.carpooling.services.contracts.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/travels")
public class TravelMvcController {
    private final TravelService travelService;
    private final TravelRequestService travelRequestService;
    private final UserService userService;
    private final TravelMapper travelMapper;
    private final AuthenticationHelper authenticationHelper;

    public TravelMvcController(TravelService travelService,
                               TravelRequestService travelRequestService, UserService userService, TravelMapper travelMapper,
                               AuthenticationHelper authenticationHelper) {
        this.travelService = travelService;
        this.travelRequestService = travelRequestService;
        this.userService = userService;
        this.travelMapper = travelMapper;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping("/search")
    public String searchTravels(
            @RequestParam(required = false) String departurePoint,
            @RequestParam(required = false) String arrivalPoint,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureTime,
            @RequestParam(required = false) Short freeSpots,
            Model model) {
        List<TravelFrontEndView> travels = travelService.
                findBySearchCriteria(departurePoint, arrivalPoint, departureTime, freeSpots)
                .stream()
                .map(travelMapper::fromTravelToFrontEnd)
                .filter(travelFrontEndView -> travelFrontEndView.getStatus() == TravelStatus.PLANNED)
                .toList();
        model.addAttribute("travels", travels);
        return "TravelsView";
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
        model.addAttribute("travels", travels);
        model.addAttribute("travelsAsPassenger", travelsAsPassenger);
        //ToDo View
        return "UserTravelsView";
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
        model.addAttribute("travels", travels);
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
        List<TravelRequest> travelRequests = travelService.getById(id)
                .getTravelRequests()
                .stream()
                .filter(travelRequest -> travelRequest.getStatus()==TravelRequestStatus.PENDING)
                .toList();
        model.addAttribute("startDestination", travelFrontEndView.getDeparturePoint());
        model.addAttribute("endDestination", travelFrontEndView.getArrivalPoint());
        model.addAttribute("travel", travelFrontEndView);
        model.addAttribute("passengers", travelService.getAllPassengersForTravel(travelService.getById(id)));
        model.addAttribute("travelRequestForThisTravel",travelRequests);
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
        } catch (InvalidLocationException | InvalidTravelException e) {
            errors.rejectValue("departurePoint", "location_error", e.getMessage());
            errors.rejectValue("arrivalPoint", "location_error", e.getMessage());
            return "NewTravelView";
        }
        return "redirect:/travels/" + newTravel.getId();
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
        model.addAttribute("travelId", id);

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
        Travel travelUpdate = travelMapper.toTravelFromTravelUpdateSaveDto(travel, travelUpdateDto);
        if (errors.hasErrors()) {
            return "UpdateTravelView";
        }

        try {
            travelService.update(travelUpdate, loggedUser);
        } catch (AuthorizationException | InvalidOperationException e) {
            return "AccessDeniedView";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (InvalidLocationException e) {
            errors.rejectValue("departurePoint", "location_error", e.getMessage());
            errors.rejectValue("arrivalPoint", "location_error", e.getMessage());
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
    public String cancelTravel(@PathVariable Long id, HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            Travel travel = travelService.getById(id);
            travelService.cancelTravel(id, loggedUser);
            return "redirect:/travels/user";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (InvalidOperationException | AuthorizationException e) {
            return "AccessDeniedView";
        }
    }

    @GetMapping("/{id}/apply")
    public String applyForTravel(@PathVariable Long id, HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            Travel travel = travelService.getById(id);
            if (travelRequestService.existsTravelRequestByTravelAndPassengerAndStatus(travel,
                    loggedUser,
                    TravelRequestStatus.PENDING)) {
                travelRequestService.delete(travelRequestService.findByTravelIsAndPassengerIsAndStatus(
                        travel, loggedUser, TravelRequestStatus.PENDING).getId(), loggedUser
                );
                return "redirect:/travels/" + travel.getId();
            }
            travelRequestService.createRequest(travel, loggedUser);
            return "redirect:/travels/" + travel.getId();
        } catch (
                AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (
                EntityNotFoundException e) {
            return "NotFoundView";
        } catch (
                VehicleIsFullException e) {
            return "VehicleIsFullView";
        } catch (InvalidOperationException |
                 DuplicateEntityException e) {
            return "InvalidOperationView";
        }

    }
    @GetMapping("{id}/approve/user/{userId}")
    public String approveRequest(@PathVariable Long id ,@PathVariable Long userId , HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            User requestCreator = userService.getById(userId);
            Travel travel = travelService.getById(id);
            travelRequestService.approveRequest(travel,loggedUser,requestCreator);
            return "redirect:/travels/"+travel.getId();
        } catch (EntityNotFoundException e) {
            return "NotFoudView";
        } catch (VehicleIsFullException e) {
            return "VehicleIsFullView";
        } catch (AuthorizationException e) {
            return "AccessDeniedView";
        }
    }

    @GetMapping("{id}/reject")
    public String rejectRequest(@PathVariable Long id, HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            Travel travel = travelService.getById(id);
            travelRequestService.rejectRequest(travel, loggedUser);
            return "redirect:/";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (AuthorizationException e) {
            return "AccessDeniedView";
        } catch (EntityNotFoundException e) {
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

    @ModelAttribute("travelRequests")
    public List<TravelRequest> populateTravelRequests() {
        return travelRequestService.get();
    }

    @ModelAttribute("latestTravels")
    public List<Travel> populateLatestTravels() {
        return travelService.findLatestTravels();
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
