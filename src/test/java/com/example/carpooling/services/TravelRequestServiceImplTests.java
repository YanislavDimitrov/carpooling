package com.example.carpooling.services;

import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.exceptions.InvalidOperationException;
import com.example.carpooling.exceptions.VehicleIsFullException;
import com.example.carpooling.models.Passenger;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.repositories.contracts.PassengerRepository;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.repositories.contracts.TravelRequestRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


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
    @Mock
    private PassengerRepository passengerRepository;

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

    @Test
    public void testCreateeeRequestVehicleIsFull() {
        Travel travel = new Travel();
        User user = new User();
        travel.setFreeSpots((short) 0);
        when(travelRepository.existsById(travel.getId())).thenReturn(true);
        when(userRepository.existsById(user.getId())).thenReturn(true);
        assertThrows(VehicleIsFullException.class, () -> travelRequestService.createRequest(travel, user));
    }

    @Test
    public void testExistsTravelRequestByTravelAndPassengerAndStatus() {
        Travel travel = new Travel();
        User user = new User();
        TravelRequestStatus status = TravelRequestStatus.PENDING;
        when(travelRequestRepository.existsTravelRequestByTravelAndPassengerAndStatus(travel, user, status)).thenReturn(true);
        boolean result = travelRequestService.existsTravelRequestByTravelAndPassengerAndStatus(travel, user, status);
        assertTrue(result);
    }

    @Test
    public void testNotExistsTravelRequestByTravelAndPassengerAndStatus() {
        Travel travel = new Travel();
        User user = new User();
        TravelRequestStatus status = TravelRequestStatus.PENDING;
        when(travelRequestRepository.existsTravelRequestByTravelAndPassengerAndStatus(travel, user, status)).thenReturn(false);
        boolean result = travelRequestService.existsTravelRequestByTravelAndPassengerAndStatus(travel, user, status);
        assertFalse(result);
    }

    @Test
    public void testExistsByTravelAndPassenger() {
        Travel travel = new Travel();
        User user = new User();
        when(travelRequestRepository.existsByTravelAndPassenger(travel, user)).thenReturn(true);
        boolean result = travelRequestService.existsByTravelAndPassenger(travel, user);
        assertTrue(result);
    }

    @Test
    public void testNotExistsByTravelAndPassenger() {
        Travel travel = new Travel();
        User user = new User();
        when(travelRequestRepository.existsByTravelAndPassenger(travel, user)).thenReturn(false);
        boolean result = travelRequestService.existsByTravelAndPassenger(travel, user);
        assertFalse(result);
    }

    @Test
    public void testHaveTravelInTheListWhenApprovedRequestExists() {
        User user = new User();
        Travel travel = new Travel();
        TravelRequest approvedRequest = new TravelRequest();
        approvedRequest.setTravel(travel);
        approvedRequest.setStatus(TravelRequestStatus.APPROVED);

        List<TravelRequest> travelsAsPassenger = new ArrayList<>();
        travelsAsPassenger.add(approvedRequest);
        user.setTravelsAsPassenger(travelsAsPassenger);
        boolean result = travelRequestService.haveTravelInTheList(user, travel);
        assertTrue(result);
    }

    @Test
    public void testHaveTravelInTheListWhenNoApprovedRequestExists() {
        User user = new User();
        Travel travel = new Travel();
        List<TravelRequest> travelsAsPassenger = new ArrayList<>();
        user.setTravelsAsPassenger(travelsAsPassenger);
        boolean result = travelRequestService.haveTravelInTheList(user, travel);
        assertFalse(result);
    }

    @Test
    public void testDeleteByTravelAndAndPassenger_Success() {
        Travel travel = new Travel();
        travel.setId(1L);
        short freeSpots = 4 ;
        travel.setFreeSpots(freeSpots);
        User user = new User();
        user.setId(1L);
        Passenger passenger = new Passenger();
        when(travelRepository.existsById(travel.getId())).thenReturn(true);
        when(userRepository.existsById(user.getId())).thenReturn(true);
        when(passengerRepository.existsByUserAndTravel(user, travel)).thenReturn(true);
        when(passengerRepository.findByUserAndTravel(user, travel)).thenReturn(passenger);
        travelRequestService.deleteByTravelAndAndPassenger(travel, user);
        verify(travelRequestRepository).deleteByTravelAndAndPassenger(travel, user);
        verify(passengerRepository).delete(passenger);

    }

    @Test
    public void testDeleteByTravelAndAndPassenger_TravelNotFound() {
        Travel travel = new Travel();
        travel.setId(1L);
        short freeSpots = 4 ;
        travel.setFreeSpots(freeSpots);
        User user = new User();
        user.setId(1L);
        Passenger passenger = new Passenger();
        when(travelRepository.existsById(travel.getId())).thenReturn(false);
        when(userRepository.existsById(user.getId())).thenReturn(true);
        when(passengerRepository.existsByUserAndTravel(user, travel)).thenReturn(true);
        when(passengerRepository.findByUserAndTravel(user, travel)).thenReturn(passenger);
        Assertions.assertThrows(EntityNotFoundException.class,()->travelRequestService.deleteByTravelAndAndPassenger(travel,user));
    }

    @Test
    public void testDeleteByTravelAndAndPassenger_UserNotFound() {
        // Similar to the first test, but set up userRepository.existsById to return false.
        // Verify that EntityNotFoundException is thrown and other methods are not called.
        Travel travel = new Travel();
        travel.setId(1L);
        short freeSpots = 4 ;
        travel.setFreeSpots(freeSpots);
        User user = new User();
        user.setId(1L);
        Passenger passenger = new Passenger();
        when(travelRepository.existsById(travel.getId())).thenReturn(false);
        when(userRepository.existsById(user.getId())).thenReturn(false);
        when(passengerRepository.existsByUserAndTravel(user, travel)).thenReturn(true);
        when(passengerRepository.findByUserAndTravel(user, travel)).thenReturn(passenger);
        Assertions.assertThrows(EntityNotFoundException.class,()->travelRequestService.deleteByTravelAndAndPassenger(travel,user));
    }

    @Test
    public void testDeleteByTravelAndAndPassenger_UserNotPassenger() {
        Travel travel = new Travel();
        travel.setId(1L);
        short freeSpots = 4 ;
        travel.setFreeSpots(freeSpots);
        User user = new User();
        user.setId(1L);
        Passenger passenger = new Passenger();
        when(travelRepository.existsById(travel.getId())).thenReturn(false);
        when(userRepository.existsById(user.getId())).thenReturn(true);
        when(passengerRepository.existsByUserAndTravel(user, travel)).thenReturn(false);
        when(passengerRepository.findByUserAndTravel(user, travel)).thenReturn(passenger);
        Assertions.assertThrows(EntityNotFoundException.class,()->travelRequestService.deleteByTravelAndAndPassenger(travel,user));
    }

    @Test
    public void testDelete_Success() {

        Long id = 1L;
        User editor = new User();
        TravelRequest travelRequest = new TravelRequest();
        travelRequest.setPassenger(editor);

        when(travelRequestRepository.findById(id)).thenReturn(Optional.of(travelRequest));

        travelRequestService.delete(id, editor);

        verify(travelRequestRepository).delete(travelRequest);
    }

    @Test
    public void testDelete_NotAuthorized() {

        Long id = 1L;
        User editor = new User();
        User passenger = new User();
        TravelRequest travelRequest = new TravelRequest();
        travelRequest.setPassenger(passenger);

        when(travelRequestRepository.findById(id)).thenReturn(Optional.of(travelRequest));

        assertThrows(AuthorizationException.class, () -> travelRequestService.delete(id, editor));

        verify(travelRequestRepository, never()).delete(any());
    }
    @Test
    public void testUpdate_Success() {

        User editor = new User();
        TravelRequest travelRequest = new TravelRequest();
        travelRequest.setPassenger(editor);

        travelRequestService.update(travelRequest, editor);

        verify(travelRequestRepository).save(travelRequest);
    }

    @Test
    public void testUpdate_NotAuthorized() {

        User editor = new User();
        User passenger = new User();
        TravelRequest travelRequest = new TravelRequest();
        travelRequest.setPassenger(passenger);

        assertThrows(AuthorizationException.class, () -> travelRequestService.update(travelRequest, editor));

        verify(travelRequestRepository, never()).save(any());
    }
}