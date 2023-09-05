package com.example.carpooling.services;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.exceptions.VehicleIsFullException;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.repositories.contracts.TravelRequestRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


public class TravelRequestServiceImplTests {
    private static final String VEHICLE_IS_FULL = "There isn't free spot left in the vehicle";
    private static final String USER_NOT_FOUND = "User with ID %d was not found!";
    private static final String DRIVER_APPLYING_RESTRICTION = "You cannot apply to be a passenger for your own travel!";
    private static final String TRAVEL_NOT_ACTIVE = "You cannot apply for a travel which is not active!";
    private static final String REQUEST_ALREADY_SENT = "You have already sent a request to participate in this travel";

    @Mock
    private TravelRequestRepository travelRequestRepository;

    @Mock
    private TravelRepository travelRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TravelRequestServiceImpl travelRequestService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetTravelRequestByIdNotFound() {
        long requestId = 1;
        when(travelRequestRepository.findById(requestId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> travelRequestService.get(requestId));
    }

    @Test
    public void testCreateRequestVehicleIsFull() {
        Travel travel = new Travel();
        User user = new User();
        travel.setFreeSpots((short) 0);
        when(travelRepository.existsById(travel.getId())).thenReturn(true);
        assertThrows(VehicleIsFullException.class, () -> travelRequestService.createRequest(travel, user));
    }

    @Test
    public void testGetAllTravelRequests() {
        List<TravelRequest> expectedRequests = new ArrayList<>();
        expectedRequests.add(new TravelRequest());
        expectedRequests.add(new TravelRequest());
        when(travelRequestRepository.findAll()).thenReturn(expectedRequests);
        List<TravelRequest> actualRequests = travelRequestService.get();
        assertEquals(2, actualRequests.size());
    }

    @Test
    public void testGetPendingTravelRequests() {
        List<TravelRequest> expectedPendingRequests = new ArrayList<>();
        expectedPendingRequests.add(new TravelRequest());
        expectedPendingRequests.add(new TravelRequest());
        when(travelRequestRepository.findAllByStatusIs(TravelRequestStatus.PENDING)).thenReturn(expectedPendingRequests);
        List<TravelRequest> actualPendingRequests = travelRequestService.getPending();
        assertEquals(2, actualPendingRequests.size());
    }

    @Test
    public void testGetByTravel() {
        long travelId = 1;
        Travel travel = new Travel();
        travel.setId(travelId);
        List<TravelRequest> expectedRequests = new ArrayList<>();
        expectedRequests.add(new TravelRequest());
        expectedRequests.add(new TravelRequest());
        when(travelRepository.existsById(travelId)).thenReturn(true);
        when(travelRequestRepository.findByTravelIs(travel)).thenReturn(expectedRequests);
        List<TravelRequest> actualRequests = travelRequestService.getByTravel(travel);
        assertEquals(expectedRequests.size(), actualRequests.size());
    }

    @Test
    public void testGetByTravelNotFound() {
        long travelId = 1;
        Travel travel = new Travel();
        travel.setId(travelId);
        when(travelRepository.existsById(travelId)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> travelRequestService.getByTravel(travel));
    }

    @Test
    public void testFindByTravelAndPassengerAndStatus() {
        Travel travel = new Travel();
        User passenger = new User();
        TravelRequestStatus status = TravelRequestStatus.PENDING;
        TravelRequest expectedRequest = new TravelRequest();
        expectedRequest.setTravel(travel);
        expectedRequest.setPassenger(passenger);
        expectedRequest.setStatus(status);
        when(travelRequestRepository.findByTravelIsAndPassengerIsAndStatus(travel, passenger, status)).thenReturn(expectedRequest);
        TravelRequest actualRequest = travelRequestService.findByTravelIsAndPassengerIsAndStatus(travel, passenger, status);
        assertEquals(expectedRequest, actualRequest);
    }

    @Test
    public void testFindByTravelAndPassengerAndStatusNotFound() {
        Travel travel = new Travel();
        User passenger = new User();
        TravelRequestStatus status = TravelRequestStatus.PENDING;
        when(travelRequestRepository.findByTravelIsAndPassengerIsAndStatus(travel, passenger, status)).thenReturn(null);
        TravelRequest actualRequest = travelRequestService.findByTravelIsAndPassengerIsAndStatus(travel, passenger, status);
        assertNull(actualRequest);
    }

    @Test
    public void testFindByTravelAndStatus() {

        long travelId = 1;
        Travel travel = new Travel();
        travel.setId(travelId);
        TravelRequestStatus status = TravelRequestStatus.PENDING;
        List<TravelRequest> expectedRequests = new ArrayList<>();
        expectedRequests.add(new TravelRequest());
        expectedRequests.add(new TravelRequest());
        when(travelRepository.existsById(travelId)).thenReturn(true);
        when(travelRequestRepository.findByTravelIsAndStatus(travel, status)).thenReturn(expectedRequests);
        List<TravelRequest> actualRequests = travelRequestService.findByTravelIsAndStatus(travel, status);
        assertEquals(expectedRequests.size(), actualRequests.size());
    }

    @Test
    public void testFindByTravelAndStatusNotFound() {
        long travelId = 1;
        Travel travel = new Travel();
        travel.setId(travelId);
        TravelRequestStatus status = TravelRequestStatus.PENDING;
        when(travelRepository.existsById(travelId)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> travelRequestService.findByTravelIsAndStatus(travel, status));
    }


    @Test
    public void testCreateeRequestVehicleIsFull() {
        Travel travel = new Travel();
        User user = new User();
        travel.setFreeSpots((short) 0);
        when(travelRepository.existsById(travel.getId())).thenReturn(true);
        assertThrows(VehicleIsFullException.class, () -> travelRequestService.createRequest(travel, user));
    }

}