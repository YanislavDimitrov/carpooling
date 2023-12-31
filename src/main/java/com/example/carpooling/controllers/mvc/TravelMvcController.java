package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.*;
import com.example.carpooling.exceptions.duplicate.DuplicateEntityException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.helpers.ExtractionHelper;
import com.example.carpooling.helpers.mappers.FeedbackMapper;
import com.example.carpooling.helpers.mappers.TravelMapper;
import com.example.carpooling.models.*;
import com.example.carpooling.models.dtos.FeedbackCreateDto;
import com.example.carpooling.models.dtos.TravelCreationOrUpdateDto;
import com.example.carpooling.models.dtos.TravelFilterDto;
import com.example.carpooling.models.dtos.TravelFrontEndView;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.models.enums.UserStatus;
import com.example.carpooling.services.contracts.FeedbackService;
import com.example.carpooling.services.contracts.TravelRequestService;
import com.example.carpooling.services.contracts.TravelService;
import com.example.carpooling.services.contracts.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/travels")
public class TravelMvcController {
    public static final String WITHDRAWAL_REJECTED = "You cannot withdraw your request if the travel is not with status 'PLANNED'!";
    private final TravelService travelService;
    private final TravelRequestService travelRequestService;
    private final UserService userService;
    private final FeedbackService feedbackService;
    private final TravelMapper travelMapper;
    private final FeedbackMapper feedbackMapper;
    private final AuthenticationHelper authenticationHelper;
    private final ExtractionHelper extractionHelper;
    private final ModelMapper modelMapper;

    public TravelMvcController(TravelService travelService,
                               TravelRequestService travelRequestService, UserService userService, FeedbackService feedbackService, TravelMapper travelMapper,
                               FeedbackMapper feedbackMapper, AuthenticationHelper authenticationHelper, ExtractionHelper extractionHelper, ModelMapper modelMapper) {
        this.travelService = travelService;
        this.travelRequestService = travelRequestService;
        this.userService = userService;
        this.feedbackService = feedbackService;
        this.travelMapper = travelMapper;
        this.feedbackMapper = feedbackMapper;
        this.authenticationHelper = authenticationHelper;
        this.extractionHelper = extractionHelper;
        this.modelMapper = modelMapper;
    }

    @GetMapping()
    public String viewAllTravelsIncludingActive(HttpSession session,
                                                Model model, @ModelAttribute("filter") TravelFilterDto filter,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "5") int size,
                                                HttpServletRequest request) {
        try {
            authenticationHelper.tryGetUser(session);
            List<TravelFrontEndView> travels = travelService.get()
                    .stream()
//                    .map(travel -> modelMapper.map(travel, TravelFrontEndView.class))
                    .map(travel -> travelMapper.fromTravelToFrontEnd(travel))
                    .toList();

            Sort sort;
            if (filter.getSortOrder().equalsIgnoreCase("desc")) {
                sort = Sort.by(Sort.Direction.DESC, filter.getSortBy());
            } else {
                sort = Sort.by(Sort.Direction.ASC, filter.getSortBy());
            }
            Page<Travel> paginatedTravels = travelService.findAllPaginated(
                    page,
                    size,
                    filter.getFreeSpots(),
                    filter.getDepartedBefore(),
                    filter.getDepartedAfter(),
                    filter.getDeparturePoint(),
                    filter.getArrivalPoint(),
                    filter.getPrice(),
                    sort);

            prepareInformationForTheView(model, filter, request, travels, paginatedTravels);

            return "AllTravelsView";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
    }

    private void prepareInformationForTheView(Model model, @ModelAttribute("filter") TravelFilterDto filter, HttpServletRequest request, List<TravelFrontEndView> travels, Page<Travel> paginatedTravels) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String parameters = extractionHelper.extractParametersSection(parameterMap);
        model.addAttribute("filter", filter);
        model.addAttribute("travelPage", paginatedTravels);
        model.addAttribute("filterParams", parameters);
        model.addAttribute("travels", travels);
    }


    @GetMapping("/search")
    public String searchTravels(
            @RequestParam(required = false) String departurePoint,
            @RequestParam(required = false) String arrivalPoint,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureTime,
            @RequestParam(required = false) Short freeSpots,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @ModelAttribute("filter") TravelFilterDto filter,
            HttpServletRequest request) {
        List<TravelFrontEndView> travels = travelService.
                findBySearchCriteria(departurePoint, arrivalPoint, departureTime, freeSpots)
                .stream()
                .map(travelMapper::fromTravelToFrontEnd)
                .toList();
        Sort sort;
        if (filter.getSortOrder().equalsIgnoreCase("desc")) {
            sort = Sort.by(Sort.Direction.DESC, filter.getSortBy());
        } else {
            sort = Sort.by(Sort.Direction.ASC, filter.getSortBy());
        }
        Page<Travel> paginatedTravels = travelService.findAllPlannedPaginated(
                page,
                size,
                filter.getFreeSpots(),
                filter.getDepartedBefore(),
                filter.getDepartedAfter(),
                filter.getDeparturePoint(),
                filter.getArrivalPoint(),
                filter.getPrice(),
                sort);

        prepareInformationForTheView(model, filter, request, travels, paginatedTravels);


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
        Travel travel;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
            travel = travelService.getById(id);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }
        TravelFrontEndView travelFrontEndView = travelMapper.fromTravelToFrontEnd(travel);
        if (travelFrontEndView.isDeleted()) {
            return "DeletedSourceView";
        }
        List<TravelRequest> travelRequests = travelRequestService.findByTravelIsAndStatus(travel, TravelRequestStatus.PENDING);
        boolean isRequestedByUser = travelService.isRequestedByUser(id, loggedUser);
        boolean isPassenger = travelService.isPassengerInThisTravel(loggedUser, travel);
        boolean isRejected = travelService.isPassengerRejected(loggedUser, travel);
        List<User> passengers = travelService.getAllPassengersForTravel(travelService.getById(id));
        List<Feedback> feedbacksForTravel = feedbackService.findByTravelId(id);
        model.addAttribute("startDestination", travelFrontEndView.getDeparturePoint());
        model.addAttribute("endDestination", travelFrontEndView.getArrivalPoint());
        model.addAttribute("travel", travelFrontEndView);
        model.addAttribute("passengers", passengers);
        model.addAttribute("travelRequestForThisTravel", travelRequests);
        model.addAttribute("isPendingApproval", isRequestedByUser);
        model.addAttribute("isPassenger", isPassenger);
        model.addAttribute("isRejected", isRejected);
        model.addAttribute("driverId", travelService.getById(id).getDriver().getId());
        model.addAttribute("feedbacks", feedbacksForTravel);
        model.addAttribute("requestsCount", travel.getTravelRequests().size());
        model.addAttribute("passengersCount", passengers.size());

        return "TravelView";
    }

    @GetMapping("/new")
    public String showNewTravelPage(Model model, HttpSession session) {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
            if (loggedUser.getStatus() == UserStatus.BLOCKED) {
                return "BlockedUserView";
            }
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
        model.addAttribute("travel", new TravelCreationOrUpdateDto());
        model.addAttribute("vehicles", loggedUser.getVehicles().stream().filter(vehicle -> !vehicle.isDeleted()));
        model.addAttribute("loggedUser", loggedUser);
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
        if (driver.getStatus() == UserStatus.BLOCKED) {
            return "BlockedUserView";
        }
        if (!driver.isValidated()) {
            model.addAttribute("loggedUser", driver);
            return "NotValidatedUser";
        }
        if (errors.hasErrors()) {
            model.addAttribute("vehicles", driver.getVehicles().stream().filter(vehicle -> !vehicle.isDeleted()));
            return "NewTravelView";
        }
        Travel newTravel;
        try {
            newTravel = travelMapper.toTravelFromTravelCreationDto(travel);
            travelService.create(newTravel, driver);
        } catch (InvalidOperationException e) {
            errors.rejectValue("departureTime", "creation_error", e.getMessage());
            model.addAttribute("vehicles", driver.getVehicles().stream().filter(vehicle -> !vehicle.isDeleted()));
            return "NewTravelView";
        } catch (InvalidLocationException | InvalidTravelException e) {
            errors.rejectValue("departurePoint", "location_error_departure", e.getMessage());
            errors.rejectValue("arrivalPoint", "location_error_arrival", e.getMessage());
            model.addAttribute("vehicles", driver.getVehicles().stream().filter(vehicle -> !vehicle.isDeleted()));
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
            if (!travel.getDriver().equals(loggedUser)) {
                return "AccessDeniedView";
            }
            if (loggedUser.getStatus() == UserStatus.BLOCKED) {
                return "BlockedUserView";
            }
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }
        TravelCreationOrUpdateDto travelCreationOrUpdateDto = travelMapper.fromTravel(id);
        model.addAttribute("updateTravel", travelCreationOrUpdateDto);
        model.addAttribute("travelId", id);
        model.addAttribute("vehicles", loggedUser.getVehicles().stream().filter(vehicle -> !vehicle.isDeleted()));

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
        Travel travelToCheckIfValid = travelMapper.fromTravelCreateToTestDepartureTime(travelUpdateDto);
        if (loggedUser.getStatus() == UserStatus.BLOCKED) {
            return "BlockedUserView";
        }
        try {
            travelService.checkIfTheTravelTimeFrameIsValid(travel, travelToCheckIfValid, loggedUser);

        } catch (InvalidOperationException e) {
            errors.rejectValue("departureTime", "creation_error", e.getMessage());
            model.addAttribute("vehicles", loggedUser.getVehicles().stream().filter(vehicle -> !vehicle.isDeleted()));
            model.addAttribute("travelId", travel.getId());
            return "redirect:/travels/" + id + "/update";
        }

        Travel travelUpdate = travelMapper.toTravelFromTravelUpdateSaveDto(travel, travelUpdateDto);

        if (errors.hasErrors()) {
            model.addAttribute("vehicles", loggedUser.getVehicles().stream().filter(vehicle -> !vehicle.isDeleted()));
            model.addAttribute("travelId", travel.getId());
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
            model.addAttribute("vehicles", loggedUser.getVehicles().stream().filter(vehicle -> !vehicle.isDeleted()));
            model.addAttribute("travelId", travel.getId());
            return "UpdateTravelView";
        } catch (InvalidOperationException e) {
            errors.rejectValue("departureTime", "creation_error", e.getMessage());
            model.addAttribute("vehicles", loggedUser.getVehicles().stream().filter(vehicle -> !vehicle.isDeleted()));
            model.addAttribute("travelId", travel.getId());
            return "UpdateTravelView";
        }
        return "redirect:/travels/" + id;
    }

    @GetMapping("/{id}/cancel")
    public String cancelTravel(@PathVariable Long id, HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            Travel travel = travelService.getById(id);
            if (!loggedUser.equals(travel.getDriver()) && loggedUser.getRole() != UserRole.ADMIN) {
                return "AccessDeniedView";
            }
            if (loggedUser.getStatus() == UserStatus.BLOCKED) {
                return "BlockedUserView";
            }
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
            if (travel.getDriver().equals(loggedUser)) {
                throw new InvalidOperationException("Driver cannot apply as passenger");
            }
            if (travelRequestService
                    .existsTravelRequestByTravelAndPassengerAndStatus(travel, loggedUser, TravelRequestStatus.REJECTED)) {
                return "RequestRejectedView";
            }

            if (loggedUser.getStatus() == UserStatus.BLOCKED) {
                return "BlockedUserView";
            }
            travelRequestService.createRequest(travel, loggedUser);
            return String.format("redirect:/travels/%s", id);
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
            if (loggedUser.getStatus() == UserStatus.BLOCKED || requestCreator.getStatus() == UserStatus.BLOCKED) {
                return "BlockedUserView";
            }
            travelRequestService.approveRequest(travel, loggedUser, requestCreator);
            return "redirect:/travels/" + travel.getId();
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
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
            if (loggedUser.getStatus() == UserStatus.BLOCKED) {
                return "BlockedUserView";
            }
            travelRequestService.rejectRequest(travel, loggedUser, requestCreator);
            return String.format("redirect:/travels/%s", id);
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
            if (loggedUser.getStatus() == UserStatus.BLOCKED) {
                return "BlockedUserView";
            }
            travelRequestService.rejectRequestWhenUserIsAlreadyPassenger(travel, passenger, loggedUser);
            return String.format("redirect:/travels/%s", id);
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
            travelService.completeTravel(id, loggedUser);
            model.addAttribute("id", id);
            return "redirect:/travels/" + id;
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
            loggedUser = authenticationHelper.tryGetUser(session);
            if (loggedUser.getStatus() == UserStatus.BLOCKED) {
                return "BlockedUserView";
            }
            Travel travel = travelService.getById(id);
            if (travel.getDriver().equals(loggedUser)) {
                throw new InvalidOperationException("Driver cannot apply as passenger");
            }
            if (travel.getStatus() != TravelStatus.PLANNED) {
                throw new InvalidTravelException(WITHDRAWAL_REJECTED);
            }
            if (travelRequestService
                    .existsTravelRequestByTravelAndPassengerAndStatus(travel, loggedUser, TravelRequestStatus.REJECTED)) {
                return "RequestRejectedView";
            }
            boolean isPassengerInThisTravel = travelRequestService.existsByTravelAndPassenger(travel, loggedUser);
            if (isPassengerInThisTravel) {
                travelRequestService.deleteByTravelAndPassenger(travel, loggedUser);
            }
            return String.format("redirect:/travels/%s", id);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (InvalidTravelException e) {
            return "InvalidTravelStatus";
        } catch (InvalidOperationException e) {
            return "InvalidOperationView";
        }
    }

    @GetMapping("/{travelId}/new/feedback/recipient/{recipientId}")
    public String createFeedback(@PathVariable Long travelId,
                                 @PathVariable Long recipientId,
                                 HttpSession session,
                                 Model model) {
        Travel travel;
        User recipient;
        User creator;
        try {
            creator = authenticationHelper.tryGetUser(session);
            travel = travelService.getById(travelId);
            recipient = userService.getById(recipientId);
            if (creator.getStatus() == UserStatus.BLOCKED) {
                return "BlockedUserView";
            }
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }
        model.addAttribute("feedback", new FeedbackCreateDto());
        model.addAttribute("recipient", recipient);
        model.addAttribute("creator", creator);
        model.addAttribute("travel", travel);
        model.addAttribute("travelId", travel.getId());
        model.addAttribute("recipientId", recipient.getId());
        return "NewFeedbackView";
    }

    @PostMapping("/{travelId}/new/feedback/recipient/{recipientId}")
    public String createFeedback(@Valid @ModelAttribute("feedback") FeedbackCreateDto feedback,
                                 BindingResult errors,
                                 HttpSession session,
                                 @PathVariable Long travelId,
                                 @PathVariable Long recipientId) {
        if (errors.hasErrors()) {
            errors.rejectValue("comment", "rating_error");
            return "NewFeedbackView";
        }


        Travel travel;
        User recipient;
        User creator;
        Feedback createdFeedback;
        try {
            travel = travelService.getById(travelId);
            recipient = userService.getById(recipientId);
            creator = authenticationHelper.tryGetUser(session);
            createdFeedback = feedbackMapper.fromCreationDto(feedback, creator, recipient, travel);
            feedbackService.create(travel, creator, recipient, createdFeedback);
            return "redirect:/feedbacks";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (InvalidFeedbackException | TravelNotCompletedException | InvalidOperationException e) {
            errors.rejectValue("comment", "rating_error", e.getMessage());
            return "FeedbackAlreadyGivenView";
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

    @ModelAttribute("hasProfilePicture")
    public Boolean hasProfilePicture(HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            return loggedUser.getProfilePicture() != null;
        } catch (AuthenticationFailureException e) {
            return false;
        }
    }

    @ModelAttribute("isBlocked")
    public boolean populateIsBlocked(HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            return loggedUser.getStatus() == UserStatus.BLOCKED;
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
