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
    public static final String WITHDRAWAL_REJECTED = "You cannot withdraw your request if the travel is not with status 'PLANNED'!";
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

    @GetMapping
    public String viewAllTravels(Model model, HttpSession session) {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
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


    @GetMapping("/all")
    public String viewAllTravelsIncludingActive(HttpSession session, Model model) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            List<TravelFrontEndView> travels = travelService.get()
                    .stream()
                    .map(travelMapper::fromTravelToFrontEnd)
                    .toList();
            model.addAttribute("travels", travels);
            return "TravelsView";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
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
                .filter(travelFrontEndView -> !travelFrontEndView.isDeleted())
                .toList());
        List<TravelFrontEndView> travelsAsPassenger = travelService.findTravelsAsPassengerByUser(user)
                .stream()
                .map(travelMapper::fromTravelRequest)
                .map(travelMapper::fromTravelToFrontEnd)
                .toList();
        model.addAttribute("travels", travels);
        model.addAttribute("travelsAsPassenger", travelsAsPassenger);
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
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
        TravelFrontEndView travelFrontEndView = travelMapper.fromTravelToFrontEnd(travelService.getById(id));
        if (travelFrontEndView.isDeleted()) {
            return "DeletedSourceView";
        }
        List<TravelRequest> travelRequests = travelService.getById(id)
                .getTravelRequests()
                .stream()
                .filter(travelRequest -> travelRequest.getStatus() == TravelRequestStatus.PENDING)
                .toList();
        boolean isRequestedByUser = travelService.isRequestedByUser(id, loggedUser);
        boolean isPassenger = travelService.isPassengerInThisTravel(loggedUser, travelService.getById(id));
        model.addAttribute("startDestination", travelFrontEndView.getDeparturePoint());
        model.addAttribute("endDestination", travelFrontEndView.getArrivalPoint());
        model.addAttribute("travel", travelFrontEndView);
        model.addAttribute("passengers", travelService.getAllPassengersForTravel(travelService.getById(id)));
        model.addAttribute("travelRequestForThisTravel", travelRequests);
        model.addAttribute("isRequestedByUser", isRequestedByUser);
        model.addAttribute("isPassenger", isPassenger);
        return "TravelView";
    }

    @GetMapping("/new")
    public String showNewTravelPage(Model model, HttpSession session) {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
        model.addAttribute("travel", new TravelCreationOrUpdateDto());
        model.addAttribute("vehicles", loggedUser.getVehicles());
        return "NewTravelView";
    }

    @PostMapping("/new")
    public String createTravel(@Valid @ModelAttribute("travel") TravelCreationOrUpdateDto travel,
                               BindingResult errors,
                               HttpSession session, Model model) {
        User driver;
        try {
            driver = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
        if (errors.hasErrors()) {
            model.addAttribute("vehicles", driver.getVehicles());
            return "NewTravelView";
        }
        Travel newTravel = travelMapper.toTravelFromTravelCreationDto(travel);
        try {
            travelService.create(newTravel, driver);
        } catch (InvalidOperationException e) {
            errors.rejectValue("departureTime", "creation_error", e.getMessage());
            model.addAttribute("vehicles", driver.getVehicles());
            return "NewTravelView";
        } catch (InvalidLocationException | InvalidTravelException e) {
            errors.rejectValue("departurePoint", "location_error", e.getMessage());
            errors.rejectValue("arrivalPoint", "location_error", e.getMessage());
            model.addAttribute("vehicles", driver.getVehicles());
            return "NewTravelView";
        }
        return "redirect:/travels/" + newTravel.getId();
    }

    @GetMapping("/{id}/update")
    public String showUpdateTravelPage(@PathVariable Long id, HttpSession session, Model model) {
        Travel travel;
        User loggedUser;
        try {
            loggedUser = this.authenticationHelper.tryGetUser(session);
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
        model.addAttribute("vehicles", loggedUser.getVehicles());

        return "UpdateTravelView";
    }

    @PostMapping("/{id}/update")
    public String updateTravel(@Valid
                               @ModelAttribute("updateTravel") TravelCreationOrUpdateDto travelUpdateDto,
                               BindingResult errors,
                               @PathVariable Long id,
                               HttpSession session,
                               Model model
    ) {
        User loggedUser = this.authenticationHelper.tryGetUser(session);
        Travel travel = travelService.getById(id);
        Travel travelUpdate = travelMapper.toTravelFromTravelUpdateSaveDto(travel, travelUpdateDto);
        if (errors.hasErrors()) {
            model.addAttribute("vehicles", loggedUser.getVehicles());
            return "UpdateTravelView";
        }

        try {
            travelService.update(travelUpdate, loggedUser);
        } catch (AuthorizationException e) {
            return "AccessDeniedView";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (InvalidLocationException | InvalidTravelException e) {
            errors.rejectValue("departurePoint", "location_error", e.getMessage());
            errors.rejectValue("arrivalPoint", "location_error", e.getMessage());
            model.addAttribute("vehicles", loggedUser.getVehicles());
        } catch (InvalidOperationException e) {
            errors.rejectValue("departureTime", "creation_error", e.getMessage());
            model.addAttribute("vehicles", loggedUser.getVehicles());
            return "UpdateTravelView";
        }
        return "redirect:/travels/" + id;
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
        return "redirect:/travels/user";
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
                return "RequestCancelledView";
            }
            travelRequestService.createRequest(travel, loggedUser);
            return "RequestSentView";
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
    public String approveRequest(@PathVariable Long id, @PathVariable Long userId, HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            User requestCreator = userService.getById(userId);
            Travel travel = travelService.getById(id);
            travelRequestService.approveRequest(travel, loggedUser, requestCreator);
            return "redirect:/travels/" + travel.getId();
        } catch (EntityNotFoundException e) {
            return "NotFoudView";
        } catch (VehicleIsFullException e) {
            return "VehicleIsFullView";
        } catch (AuthorizationException e) {
            return "AccessDeniedView";
        }
    }

    @GetMapping("{id}/reject/user/{userId}")
    public String rejectRequest(@PathVariable Long id, @PathVariable Long userId, HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            Travel travel = travelService.getById(id);
            User requestCreator = userService.getById(userId);
            travelRequestService.rejectRequest(travel, loggedUser, requestCreator);
            return "redirect:/travels/" + travel.getId();
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (AuthorizationException e) {
            return "AccessDeniedView";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }
    }

    @GetMapping("/{id}/remove/user/{userId}")
    public String removePassengerFromTravel(@PathVariable Long id, @PathVariable Long userId, HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            User passenger = userService.getById(userId);
            Travel travel = travelService.getById(id);
            travelRequestService.rejectRequestWhenUserIsAlreadyPassenger(travel, passenger, loggedUser);
            return "redirect:/travels/" + travel.getId();
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (AuthorizationException e) {
            return "AccessDeniedView";
        } catch (InvalidOperationException e) {
            return "InvalidOperationView";
        }
    }

    @GetMapping("/{id}/complete")
    public String completeTravel(@PathVariable Long id, HttpSession session, Model model) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            Travel travel = travelService.getById(id);
            travelService.completeTravel(id, loggedUser);
            model.addAttribute("id", id);
            return "redirect:/";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (InvalidTravelException e) {
            return "UnableToCompleteView";
        }
    }

    @GetMapping("/{id}/delete-request")
    public String withdrawRequest(@PathVariable Long id, HttpSession session) {
        User loggedUser;
        try {
            Travel travel = travelService.getById(id);
            if (travel.getStatus() != TravelStatus.PLANNED) {
                throw new InvalidTravelException(WITHDRAWAL_REJECTED);
            }
            loggedUser = authenticationHelper.tryGetUser(session);
            boolean isPassengerInThisTravel = travelService.isPassengerInThisTravel(loggedUser, travel);
            if (isPassengerInThisTravel) {
                travelRequestService.deleteByTravelAndAndPassenger(travel, loggedUser);
            }
            return "WithDrawRequest";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (InvalidTravelException e) {
            return "InvalidTravelStatus";
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
