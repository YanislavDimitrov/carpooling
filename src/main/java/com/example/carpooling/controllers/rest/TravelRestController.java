package com.example.carpooling.controllers.rest;
import com.example.carpooling.exceptions.*;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.helpers.mappers.TravelMapper;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.TravelCreationOrUpdateDto;
import com.example.carpooling.models.dtos.TravelViewDto;
import com.example.carpooling.models.dtos.UserViewDto;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.services.BingMapsService;
import com.example.carpooling.services.contracts.TravelService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("api/travels")
public class TravelRestController {
    public static final String TRAVEL_NOT_FOUND = "Travel with ID %d was not found!";
    public static final String AUTHENTICATION_ERROR = "Failed to authenticate!";
    public static final String DELETED_SUCCESSFULLY = "Travel with ID %d was deleted successfully!";
    public static final String NOT_AUTHORIZED = "You are not authorized to delete this travel!";
    public static final String TRAVEL_COMPLETED = "Travel with ID %d was completed!";
    private final TravelService travelService;
    private final TravelMapper travelMapper;
    private final ModelMapper modelMapper;
    private final AuthenticationHelper authenticationHelper;
    private final BingMapsService bingMapsService;
    @Autowired
    public TravelRestController(TravelService travelService, TravelMapper travelMapper, ModelMapper modelMapper, AuthenticationHelper authenticationHelper, BingMapsService bingMapsService) {
        this.travelService = travelService;
        this.travelMapper = travelMapper;
        this.modelMapper = modelMapper;
        this.authenticationHelper = authenticationHelper;
        this.bingMapsService = bingMapsService;
    }
    @GetMapping
    public List<TravelViewDto> getAll(@RequestHeader HttpHeaders headers,
                                      @RequestParam(required = false) String driver,
                                      @RequestParam(required = false) TravelStatus status,
                                      @RequestParam(required = false) Short freeSpots,
                                      @RequestParam(required = false) LocalDateTime departureTime,
                                      @RequestParam(required = false, defaultValue = "id") String sortBy,
                                      @RequestParam(required = false, defaultValue = "asc") String sortOrder
    ) {
        Sort sort;
        try {
            if (sortOrder.equalsIgnoreCase("desc")) {
                sort = Sort.by(Sort.Direction.DESC, sortBy);
            } else {
                sort = Sort.by(Sort.Direction.ASC, sortBy);
            }
            List<Travel> filteredTravels;
            if (driver != null || status != null || freeSpots != null || departureTime != null) {
                filteredTravels = travelService.findByCriteria(driver, status, freeSpots, departureTime, sort);
            } else {
                filteredTravels = travelService.findAll(sort);
            }
            User user = authenticationHelper.tryGetUser(headers);
            return filteredTravels
                    .stream()
                    .map(travelMapper::fromTravel)
                    .toList();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AUTHENTICATION_ERROR);
        }
    }
    @GetMapping("/{id}")
    public TravelViewDto get(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            User user = authenticationHelper.tryGetUser(headers);
            Travel travel = travelService.getById(id);
            return travelMapper.fromTravel(travel);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(TRAVEL_NOT_FOUND, id));
        } catch (AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, AUTHENTICATION_ERROR);
        }
    }
    /**
     * @param id      this parameter is used to check if a travel with this id is existing
     * @param headers this parameter is used to authenticate the user who is trying to check the passengers
     * @return List<UserViewDto> which is full of passengers for the certain travel if there are any , otherwise empty list
     * @throws EntityNotFoundException        if a travel with this ID is not existing
     * @throws AuthenticationFailureException if the user is not authenticated
     */
    @GetMapping("/{id}/passengers")
    public List<UserViewDto> getPassengersForTravel(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            return getAllPassengersForTravel(id, headers);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(TRAVEL_NOT_FOUND, id));
        } catch (AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }

    }
    @GetMapping("/{id}/pending")
    public List<UserViewDto> getPendingPassengersForTravel(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            User userToAuthenticate = authenticationHelper.tryGetUser(headers);
            Travel travel = travelService.getById(id);
            List<User> passengers = travel.getTravelRequests().stream()
                    .filter(travelRequest -> travelRequest.getStatus() == TravelRequestStatus.PENDING)
                    .map(TravelRequest::getPassenger)
                    .toList();
            return passengers.stream().map(user -> {
                return this.modelMapper.map(user, UserViewDto.class);
            }).collect(Collectors.toList());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(TRAVEL_NOT_FOUND, id));
        } catch (AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
    @GetMapping("/{id}/rejected")
    public List<UserViewDto> getRejectedPassengersForTravel(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            User userToAuthenticate = authenticationHelper.tryGetUser(headers);
            Travel travel = travelService.getById(id);
            List<User> passengers = travel.getTravelRequests().stream()
                    .filter(travelRequest -> travelRequest.getStatus() == TravelRequestStatus.REJECTED)
                    .map(TravelRequest::getPassenger)
                    .toList();
            return passengers.stream().map(user -> {
                UserViewDto dto = this.modelMapper.map(user, UserViewDto.class);
                return dto;
            }).collect(Collectors.toList());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(TRAVEL_NOT_FOUND, id));
        } catch (AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
    @GetMapping("/travel-distance")
    public String getTravelDistance(@RequestParam String origin, @RequestParam String destination) {
        return bingMapsService.getTravelDistance(origin, destination);
    }
    @GetMapping("/location-coordinates")
    public String getLocationCoordinates(@RequestParam String address) {
        String locationJson = bingMapsService.getLocationJson(address);
        double[] coordinates = bingMapsService.parseCoordinates(locationJson);
        return "Latitude: " + coordinates[0] + ", Longitude: " + coordinates[1];
    }
    @PostMapping
    public TravelViewDto create(@RequestBody TravelCreationOrUpdateDto travelCreationOrUpdateDto, @RequestHeader HttpHeaders headers) {
        try {
            User user = authenticationHelper.tryGetUser(headers);
            Travel travel = travelMapper.toTravelFromTravelCreationDto(travelCreationOrUpdateDto);
            travelService.create(travel, user);
            return travelMapper.fromTravel(travel);
        } catch (AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
    @PutMapping("/{id}")
    public TravelViewDto update(@PathVariable Long id, @RequestBody TravelCreationOrUpdateDto travelUpdateDto, @RequestHeader HttpHeaders headers) {
        try {
            User user = authenticationHelper.tryGetUser(headers);
            Travel travel = travelService.getById(id);
            return travelMapper.fromTravel(travelService
                    .update(travelMapper.toTravelFromTravelUpdateSaveDto(travel, travelUpdateDto), user));
        } catch (AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthorizationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, NOT_AUTHORIZED);
        }
    }
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            User editor = authenticationHelper.tryGetUser(headers);
            travelService.delete(id, editor);
            return String.format(DELETED_SUCCESSFULLY, id);
        } catch (AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthorizationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, NOT_AUTHORIZED);
        }
    }
    @PutMapping("/{id}/cancel")
    public String cancelTravel(@PathVariable Long id , @RequestHeader HttpHeaders headers) {
        try {
            User user = authenticationHelper.tryGetUser(headers);
            travelService.cancelTravel(id,user);
            return "Canceled successfully";
        } catch (AuthenticationFailureException | AuthorizationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,e.getMessage());
        } catch (InvalidOperationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
        }catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }
    @PutMapping("/{id}/complete")
    public String completeTravel(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            User user = authenticationHelper.tryGetUser(headers);
            Travel travel = travelService.getById(id);
            travelService.completeTravel(id, user);
            return String.format(TRAVEL_COMPLETED, id);
        } catch (AuthenticationFailureException | AuthorizationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (InvalidOperationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }
    private static List<User> convertTravelRequestToListOfPendingUsers(Travel travel) {
        List<User> passengers = travel.getTravelRequests().stream()
                .filter(travelRequest -> travelRequest.getStatus() == TravelRequestStatus.PENDING)
                .map(TravelRequest::getPassenger)
                .collect(Collectors.toList());
        return passengers;
    }
    private List<UserViewDto> getAllPassengersForTravel(Long id, HttpHeaders headers) {
        User userToAuthenticate = authenticationHelper.tryGetUser(headers);
        Travel travel = travelService.getById(id);
        List<User> passengers = travel.getTravelRequests().stream()
                .filter(travelRequest -> travelRequest.getStatus() == TravelRequestStatus.APPROVED)
                .map(TravelRequest::getPassenger)
                .toList();
        return passengers.stream().map(user -> {
            UserViewDto dto = this.modelMapper.map(user, UserViewDto.class);
            return dto;
        }).collect(Collectors.toList());
    }
}
