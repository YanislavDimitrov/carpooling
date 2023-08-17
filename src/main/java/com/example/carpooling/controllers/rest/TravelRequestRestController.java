package com.example.carpooling.controllers.rest;

import com.example.carpooling.exceptions.*;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.helpers.mappers.TravelRequestMapper;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.TravelRequestViewDto;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.services.contracts.TravelRequestService;
import com.example.carpooling.services.contracts.TravelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/travel-requests")
public class TravelRequestRestController {
    private final TravelRequestService travelRequestService;
    private final TravelService travelService;

    private final AuthenticationHelper authenticationHelper;

    private final TravelRequestMapper travelRequestMapper;
    public static final String REQUEST_CREATED_SUCCESSFULLY = "Your request has been made successfully!You will receive answer from the driver soon!Thank you for choosing us!";

    public TravelRequestRestController(TravelRequestService travelRequestService,
                                       TravelService travelService, AuthenticationHelper authenticationHelper,
                                       TravelRequestMapper travelRequestMapper) {
        this.travelRequestService = travelRequestService;
        this.travelService = travelService;
        this.authenticationHelper = authenticationHelper;
        this.travelRequestMapper = travelRequestMapper;
    }
    @GetMapping
    public List<TravelRequestViewDto> get(@RequestHeader HttpHeaders headers) {
        try {
            User user = authenticationHelper.tryGetUser(headers);
            return travelRequestService.getPending()
                    .stream()
                    .map(travelRequestMapper::toViewDto)
                    .collect(Collectors.toList());
        } catch (AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,e.getMessage());
        }
    }
    @GetMapping("/travels/{travelId}")
    public List<TravelRequestViewDto> getByTravel(@RequestHeader HttpHeaders headers , @PathVariable Long travelId) {
        try {
            User user = authenticationHelper.tryGetUser(headers);
            Travel travel = travelService.getById(travelId);
            return  travelRequestService.getByTravel(travel)
                    .stream()
                    .filter(travelRequest -> travelRequest.getStatus() == TravelRequestStatus.PENDING)
                    .map(travelRequestMapper::toViewDto)
                    .collect(Collectors.toList());
        }catch (AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,e.getMessage());
        }catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }
    @PostMapping("/{id}/apply")
    public String applyForTravel(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            User user = authenticationHelper.tryGetUser(headers);
            Travel travel = travelService.getById(id);
            travelRequestService.createRequest(travel, user);
            return REQUEST_CREATED_SUCCESSFULLY;
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (VehicleIsFullException | DuplicateEntityException | InvalidOperationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    // ToDo - to ask whether I should add the ID of the request as path variable or with body/parameter to add the username of the passenger
    @PostMapping("/approve/{id}")
    public String approveRequest(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            TravelRequest travelRequest = travelRequestService.get(id);
            User user = authenticationHelper.tryGetUser(headers);
            travelRequestService.approveRequest(travelRequest.getId(), user);
            return "The request for travel was approved!";
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthenticationFailureException | AuthorizationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @PostMapping("/reject/{id}")
    public String rejectRequest(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            TravelRequest travelRequest = travelRequestService.get(id);
            User user = authenticationHelper.tryGetUser(headers);
            travelRequestService.rejectRequest(id, user);
            return "Your request for travel was rejected by the driver!";
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthenticationFailureException | AuthorizationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }


}
