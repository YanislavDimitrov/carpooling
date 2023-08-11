package com.example.carpooling.controllers.rest;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.helpers.mappers.TravelMapper;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.TravelViewDto;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.services.BingMapsService;
import com.example.carpooling.services.contracts.TravelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/travels")
public class TravelRestController {

    public static final String TRAVEL_NOT_FOUND = "Travel with ID %d was not found!";
    public static final String AUTHENTICATION_ERROR = "Failed to authenticate!";
    private final TravelService travelService;
    private final TravelMapper travelMapper;
    private final AuthenticationHelper authenticationHelper;

    private final BingMapsService bingMapsService;


    @Autowired
    public TravelRestController(TravelService travelService, TravelMapper travelMapper, AuthenticationHelper authenticationHelper, BingMapsService bingMapsService) {
        this.travelService = travelService;
        this.travelMapper = travelMapper;
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
                sort = Sort.by(Sort.Direction.ASC,sortBy);
            }
            List<Travel> filteredTravels;
            if (driver != null || status != null || freeSpots != null || departureTime != null) {
                filteredTravels = travelService.findByCriteria(driver,status,freeSpots,departureTime,sort);
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
}
