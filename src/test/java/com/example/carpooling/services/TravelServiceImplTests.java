package com.example.carpooling.services;

import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.exceptions.InvalidOperationException;
import com.example.carpooling.models.Passenger;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.repositories.contracts.PassengerRepository;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.repositories.contracts.TravelRequestRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.BingMapsService;
import com.example.carpooling.services.contracts.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.carpooling.services.TravelServiceImpl.checkIfTheTravelTimeFrameIsValid;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TravelServiceImplTests {
    private static final String EXISTING_TRAVEL = "Existing Travel Exception";


    @InjectMocks
    private TravelServiceImpl travelService;

    @Mock
    private TravelRepository travelRepository;

    @Mock
    private PassengerRepository passengerRepository;
    @Mock
    private TravelRequestRepository travelRequestRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;
    @Mock
    private BingMapsService bingMapsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGet() {
        Travel travel1 = new Travel();
        Travel travel2 = new Travel();
        when(travelRepository.getAll()).thenReturn(Arrays.asList(travel1, travel2));
        List<Travel> travels = travelService.get();
        assertEquals(2, travels.size());
        assertEquals(travel1, travels.get(0));
        assertEquals(travel2, travels.get(1));
    }

    @Test
    public void testGetById_ExistingTravel() {
        Long travelId = 1L;
        Travel expectedTravel = new Travel();
        when(travelRepository.findById(travelId)).thenReturn(Optional.of(expectedTravel));
        Travel travel = travelService.getById(travelId);
        assertEquals(expectedTravel, travel);
    }

    @Test
    public void testGetById_NonExistingTravel() {
        Long travelId = 1L;
        when(travelRepository.findById(travelId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> travelService.getById(travelId));
    }

    @Test
    public void testGetAllCompleted_NoCompletedTravels() {
        when(travelRepository.getAllByStatusIs(TravelStatus.COMPLETED)).thenReturn(new ArrayList<>());
        List<Travel> completedTravels = travelService.getAllCompleted();
        assertEquals(0, completedTravels.size());
    }

    @Test
    public void testGetAllCompleted_WithCompletedTravels() {
        List<Travel> completedTravels = new ArrayList<>();
        completedTravels.add(new Travel());
        completedTravels.add(new Travel());
        when(travelRepository.getAllByStatusIs(TravelStatus.COMPLETED)).thenReturn(completedTravels);
        List<Travel> returnedTravels = travelService.getAllCompleted();
        assertEquals(completedTravels.size(), returnedTravels.size());
    }

    @Test
    public void testFindAllByStatusPlanned_NoPlannedTravels() {
        when(travelRepository.getAllByStatusIs(TravelStatus.PLANNED)).thenReturn(new ArrayList<>());
        List<Travel> plannedTravels = travelService.findAllByStatusPlanned();
        assertEquals(0, plannedTravels.size());
    }

    @Test
    public void testFindAllByStatusPlanned_WithPlannedTravels() {
        List<Travel> plannedTravels = new ArrayList<>();
        plannedTravels.add(new Travel());
        plannedTravels.add(new Travel());
        when(travelRepository.getAllByStatusIs(TravelStatus.PLANNED)).thenReturn(plannedTravels);
        List<Travel> returnedTravels = travelService.findAllByStatusPlanned();
        assertEquals(plannedTravels.size(), returnedTravels.size());
    }

    @Test
    public void testFindAllPlannedPaginated_NoPlannedTravels() {
        when(travelRepository.findAllPlannedPaginated((PageRequest) any(Pageable.class), any(Sort.class), any(Short.class), any(LocalDate.class), any(LocalDate.class), anyString(), anyString(), anyString()))
                .thenReturn(Page.empty());
        Page<Travel> resultPage = travelService.findAllPlannedPaginated(0,
                10,
                (short) 2,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "Start",
                "End",
                "10",
                Sort.by(Sort.Order.asc("departureDate")));
        assertEquals(0, resultPage.getTotalElements());
    }

    @Test
    public void testFindAllPlannedPaginated_WithPlannedTravels() {
        List<Travel> plannedTravels = new ArrayList<>();
        plannedTravels.add(new Travel());
        plannedTravels.add(new Travel());
        Page<Travel> page = new PageImpl<>(plannedTravels);
        when(travelRepository.findAllPlannedPaginated((PageRequest) any(Pageable.class), any(Sort.class), any(Short.class), any(LocalDate.class), any(LocalDate.class), anyString(), anyString(), anyString()))
                .thenReturn(page);
        Page<Travel> resultPage = travelService.findAllPlannedPaginated(0,
                10,
                (short) 2,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "Start",
                "End",
                "10",
                Sort.by(Sort.Order.asc("departureDate")));
        assertEquals(plannedTravels.size(), resultPage.getTotalElements());
    }

    @Test
    public void testCreate() {
        Travel travel = new Travel();
        User driver = new User();
        travel.setDepartureTime(LocalDateTime.now().plusHours(1));
        travelService.create(travel, driver);
        verify(travelRepository, times(1)).save(travel);
    }

    @Test
    public void testFindByCriteria() {
        String driver = "John";
        TravelStatus status = TravelStatus.PLANNED;
        Short freeSpots = 2;
        LocalDateTime departureTime = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Order.asc("departureTime"));
        List<Travel> expectedTravels = Arrays.asList(new Travel(), new Travel());
        when(travelRepository.findByCriteria(driver, status, freeSpots, departureTime, sort))
                .thenReturn(expectedTravels);
        List<Travel> travels = travelService.findByCriteria(driver, status, freeSpots, departureTime, sort);
        assertEquals(expectedTravels.size(), travels.size());
        assertEquals(expectedTravels, travels);
    }

    @Test
    public void testGetAllPassengersForTravel_NoPassengers() {
        Travel travel = new Travel();
        when(passengerRepository.findAllByTravelIs(travel)).thenReturn(new ArrayList<>());
        List<User> result = travelService.getAllPassengersForTravel(travel);
        assertEquals(0, result.size());
    }

    @Test
    public void testCheckIfTheTravelTimeFrameIsValid_NoConflicts() {
        Travel travel = new Travel();
        User driver = new User();
        assertDoesNotThrow(() -> checkIfTheTravelTimeFrameIsValid(travel, driver));
    }

    @Test
    public void testCheckIfTheTravelTimeFrameIsValid_PlannedTravelConflict() {
        Travel travel = new Travel();
        travel.setDepartureTime(LocalDateTime.now());
        travel.setEstimatedTimeOfArrival(LocalDateTime.now().plusHours(2));
        travel.setStatus(TravelStatus.PLANNED);
        Travel conflictingTravel = new Travel();
        conflictingTravel.setDepartureTime(LocalDateTime.now().plusHours(1));
        conflictingTravel.setEstimatedTimeOfArrival(LocalDateTime.now().plusHours(3));
        conflictingTravel.setStatus(TravelStatus.PLANNED);
        User driver = new User();
        driver.getTravelsAsDriver().add(conflictingTravel);
        assertThrows(InvalidOperationException.class, () -> checkIfTheTravelTimeFrameIsValid(travel, driver));
    }

    @Test
    public void testCheckIfTheTravelTimeFrameIsValid_ActiveTravelConflict() {
        Travel travel = new Travel();
        travel.setDepartureTime(LocalDateTime.now());
        travel.setEstimatedTimeOfArrival(LocalDateTime.now().plusHours(2));
        travel.setStatus(TravelStatus.ACTIVE);
        Travel conflictingTravel = new Travel();
        conflictingTravel.setDepartureTime(LocalDateTime.now().plusHours(1));
        conflictingTravel.setEstimatedTimeOfArrival(LocalDateTime.now().plusHours(3));
        conflictingTravel.setStatus(TravelStatus.ACTIVE);
        User driver = new User();
        driver.getTravelsAsDriver().add(conflictingTravel);
        assertThrows(InvalidOperationException.class, () -> checkIfTheTravelTimeFrameIsValid(travel, driver));
    }

    @Test
    public void testGetAllPassengersForTravel_WithPassengers() {
        Travel travel = new Travel();
        Passenger passenger1 = new Passenger();
        Passenger passenger2 = new Passenger();
        List<Passenger> passengers = new ArrayList<>();
        passengers.add(passenger1);
        passengers.add(passenger2);
        when(passengerRepository.findAllByTravelIs(travel)).thenReturn(passengers);
        List<User> result = travelService.getAllPassengersForTravel(travel);
        assertEquals(2, result.size());
    }

    @Test
    public void testCount() {
        long expectedCount = 10L;
        when(travelRepository.count()).thenReturn(expectedCount);
        long actualCount = travelService.count();
        assertEquals(expectedCount, actualCount);
        verify(travelRepository, times(1)).count();
    }

    @Test
    public void testFindBySearchCriteria() {
        String departurePoint = "A";
        String arrivalPoint = "B";
        LocalDateTime departureTime = LocalDateTime.now();
        Short freeSpots = 2;
        List<Travel> expectedTravels = Arrays.asList(new Travel(), new Travel());
        when(travelRepository.findByCustomSearchFilter(departurePoint, arrivalPoint, departureTime, freeSpots))
                .thenReturn(expectedTravels);

        List<Travel> travels = travelService.findBySearchCriteria(departurePoint, arrivalPoint, departureTime, freeSpots);
        assertEquals(expectedTravels.size(), travels.size());
        assertEquals(expectedTravels, travels);
    }

    @Test
    public void testCompleteTravel_TravelNotFound() {
        Long travelId = 1L;
        User editor = new User();
        when(travelRepository.existsById(travelId)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> travelService.completeTravel(travelId, editor));
    }

    @Test
    public void testCheckIfTheTravelTimeFrameIsValidWithValidInput() {
        // Arrange
        User driver = new User();
        Travel oldTravel = new Travel();
        oldTravel.setDepartureTime(LocalDateTime.of(2023, 9, 1, 10, 0));
        oldTravel.setEstimatedTimeOfArrival(LocalDateTime.of(2023, 9, 1, 12, 0));

        Travel newTravel = new Travel();
        newTravel.setDepartureTime(LocalDateTime.of(2023, 9, 1, 9, 30));

        List<Travel> driverTravels = new ArrayList<>();
        driverTravels.add(oldTravel);
        when(travelRepository.findByDriver_Id(driver.getId())).thenReturn(driverTravels);

        // Act and Assert
        assertDoesNotThrow(() -> travelService.checkIfTheTravelTimeFrameIsValid(oldTravel, newTravel, driver));
    }

    @Test
    public void testCheckkIfTheTravelTimeFrameIsValidWithPassengerTravel() {
        // Arrange
        User driver = new User();
        Travel oldTravel = new Travel();
        oldTravel.setDepartureTime(LocalDateTime.of(2023, 9, 1, 10, 0));
        oldTravel.setEstimatedTimeOfArrival(LocalDateTime.of(2023, 9, 1, 12, 0));

        Travel newTravel = new Travel();
        newTravel.setDepartureTime(LocalDateTime.of(2023, 9, 1, 9, 30));

        List<Travel> driverTravels = new ArrayList<>();
        driverTravels.add(oldTravel);

        when(travelRepository.findByDriver_Id(driver.getId())).thenReturn(driverTravels);

        // Simulate passenger requests (if applicable in your application)
        List<TravelRequest> passengerRequests = new ArrayList<>();
        TravelRequest passengerRequest = new TravelRequest();
        passengerRequest.setStatus(TravelRequestStatus.APPROVED);
        passengerRequest.setTravel(oldTravel);
        passengerRequests.add(passengerRequest);

        // Act and Assert
        assertDoesNotThrow(() -> travelService.checkIfTheTravelTimeFrameIsValid(oldTravel, newTravel, driver));
    }

    @Test
    public void testtCompleteActiveTravels() {
        User user = new User();
        Travel activeTravel1 = new Travel();
        activeTravel1.setStatus(TravelStatus.ACTIVE);
        Travel activeTravel2 = new Travel();
        activeTravel2.setStatus(TravelStatus.ACTIVE);
        List<Travel> activeTravels = new ArrayList<>();
        activeTravels.add(activeTravel1);
        activeTravels.add(activeTravel2);
        user.setTravelsAsDriver(activeTravels);
        travelService.completeActiveTravels(user);
        for (Travel travel : activeTravels) {
            assertEquals(TravelStatus.COMPLETED, travel.getStatus());
            assertEquals(true, travel.isDeleted());
        }
    }

    @Test
    public void testCompleteActiveTravels() {
        User user = new User();
        user.setUserName("testUser");
        Travel travel1 = new Travel();
        travel1.setId(1L);
        travel1.setDriver(user);
        travel1.setStatus(TravelStatus.ACTIVE);
        travel1.setDeleted(false);
        Travel travel2 = new Travel();
        travel2.setId(2L);
        travel2.setDriver(user);
        travel2.setStatus(TravelStatus.PLANNED);
        travel2.setDeleted(false);
        List<Travel> travels = new ArrayList<>();
        travels.add(travel1);
        travels.add(travel2);
        user.setTravelsAsDriver(travels);
        TravelServiceImpl.completeActiveTravels(user);
        assertEquals(TravelStatus.COMPLETED, travel1.getStatus());
        assertEquals(true, travel1.isDeleted());
        assertEquals(TravelStatus.PLANNED, travel2.getStatus());
        assertEquals(false, travel2.isDeleted());
    }

    @Test
    public void testCheckIfTheTravelTimeFrameIsValid_ThrowsException() {
        Travel travel = new Travel();
        travel.setDepartureTime(LocalDateTime.now().plusHours(1));
        travel.setEstimatedTimeOfArrival(LocalDateTime.now().plusHours(2));
        travel.setStatus(TravelStatus.ACTIVE);
        Travel travelToCheck = new Travel();
        travelToCheck.setDepartureTime(LocalDateTime.now());
        travelToCheck.setEstimatedTimeOfArrival(LocalDateTime.now().plusHours(1));
        travelToCheck.setStatus(TravelStatus.ACTIVE);
        List<Travel> driverTravels = new ArrayList<>();
        driverTravels.add(travelToCheck);
        User driver = new User();
        driver.setTravelsAsDriver(driverTravels);
        when(travelRepository.existsById(anyLong())).thenReturn(true);
        assertThrows(InvalidOperationException.class, () -> checkIfTheTravelTimeFrameIsValid(travel, driver));
    }

    @Test
    public void testCheckIfTheTravellTimeFrameIsValid_ThrowsException() {
        Travel oldTravel = new Travel();
        Travel travel = new Travel();
        travel.setDepartureTime(LocalDateTime.now().plusHours(1));
        travel.setEstimatedTimeOfArrival(LocalDateTime.now().plusHours(2));
        travel.setStatus(TravelStatus.ACTIVE);
        Travel travelToCheck = new Travel();
        travelToCheck.setDepartureTime(LocalDateTime.now());
        travelToCheck.setEstimatedTimeOfArrival(LocalDateTime.now().plusHours(1));
        travelToCheck.setStatus(TravelStatus.ACTIVE);
        List<Travel> driverTravels = new ArrayList<>();
        driverTravels.add(travelToCheck);
        User driver = new User();
        driver.setTravelsAsDriver(driverTravels);
        when(travelRepository.existsById(anyLong())).thenReturn(true);
        assertThrows(InvalidOperationException.class, () -> travelService.checkIfTheTravelTimeFrameIsValid(oldTravel, travel, driver));
    }

    @Test
    public void testIsPassengerInThisTravel_PassengerExists() {
        User user = new User();
        Travel travel = new Travel();
        when(passengerRepository.existsByUserAndTravel(user, travel)).thenReturn(true);
        boolean result = travelService.isPassengerInThisTravel(user, travel);
        assertEquals(true, result);
    }

    @Test
    public void testIsPassengerInThisTravel_PassengerDoesNotExist() {
        User user = new User();
        Travel travel = new Travel();
        when(passengerRepository.existsByUserAndTravel(user, travel)).thenReturn(false);
        boolean result = travelService.isPassengerInThisTravel(user, travel);
        assertEquals(false, result);
    }

    @Test
    public void testFindAllPaginated() {
        int page = 0;
        int size = 10;
        Short freeSpots = 2;
        LocalDate departedBefore = LocalDate.of(2023, 9, 1);
        LocalDate departedAfter = LocalDate.of(2023, 8, 1);
        String departurePoint = "Point A";
        String arrivalPoint = "Point B";
        String price = "100";
        Sort sort = Sort.by(Sort.Direction.ASC, "departureDate");
        List<Travel> travelList = new ArrayList<>();
        travelList.add(new Travel());
        travelList.add(new Travel());
        Page<Travel> expectedPage = new PageImpl<>(travelList);
        when(travelRepository.findAllPaginated(
                PageRequest.of(page, size),
                sort,
                freeSpots,
                departedBefore,
                departedAfter,
                departurePoint,
                arrivalPoint,
                price))
                .thenReturn(expectedPage);
        Page<Travel> result = travelService.findAllPaginated(
                page,
                size,
                freeSpots,
                departedBefore,
                departedAfter,
                departurePoint,
                arrivalPoint,
                price,
                sort);
        verify(travelRepository).findAllPaginated(
                PageRequest.of(page, size),
                sort,
                freeSpots,
                departedBefore,
                departedAfter,
                departurePoint,
                arrivalPoint,
                price);
        assertEquals(expectedPage, result);
    }

    @Test
    public void testFindAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "departureDate");
        List<Travel> travelList = new ArrayList<>();
        travelList.add(new Travel());
        travelList.add(new Travel());
        when(travelRepository.findAll(sort)).thenReturn(travelList);
        List<Travel> result = travelService.findAll(sort);
        verify(travelRepository).findAll(sort);
        assertEquals(travelList, result);
    }

    @Test
    @Transactional
    public void testDelete_NonAdminUser_OperationDenied() {
        Long travelId = 1L;
        User nonAdminUser = new User();
        nonAdminUser.setRole(UserRole.USER);
        Travel travel = new Travel();
        User differentUser = new User(); // A different user as the driver
        travel.setDriver(differentUser);
        when(userService.getById(anyLong())).thenReturn(nonAdminUser);
        when(travelRepository.existsById(travelId)).thenReturn(true);
        when(travelRepository.findById(travelId)).thenReturn(java.util.Optional.of(travel));
        org.junit.jupiter.api.Assertions.assertThrows(AuthorizationException.class, () -> {
            travelService.delete(travelId, nonAdminUser);
        });
        verify(travelRepository, never()).deleteById(travelId);
    }

    @Test
    @Transactional
    public void testDelete_NonExistentTravel_EntityNotFoundException() {
        Long travelId = 1L;
        User nonAdminUser = new User();
        nonAdminUser.setRole(UserRole.USER);
        when(userService.getById(anyLong())).thenReturn(nonAdminUser);
        when(travelRepository.existsById(travelId)).thenReturn(false);
        org.junit.jupiter.api.Assertions.assertThrows(EntityNotFoundException.class, () -> {
            travelService.delete(travelId, nonAdminUser);
        });

        verify(travelRepository, never()).deleteById(travelId);
    }

    @Test
    public void testFindLatestTravels() {
        List<Travel> sampleTravels = new ArrayList<>();
        sampleTravels.add(createTravel(1L, LocalDateTime.now().plusDays(1)));
        sampleTravels.add(createTravel(2L, LocalDateTime.now().minusDays(1)));
        sampleTravels.add(createTravel(3L, LocalDateTime.now().plusHours(6)));
        sampleTravels.add(createTravel(4L, LocalDateTime.now().plusDays(2)));
        sampleTravels.add(createTravel(5L, LocalDateTime.now().plusDays(3)));
        when(travelRepository.findTop5ByOrderByDepartureTimeDesc()).thenReturn(sampleTravels);
        List<Travel> latestTravels = travelService.findLatestTravels();
        assertEquals(5, latestTravels.size());
        assertEquals(sampleTravels, latestTravels);
    }

    private Travel createTravel(Long id, LocalDateTime departureTime) {
        Travel travel = new Travel();
        travel.setId(id);
        travel.setDepartureTime(departureTime);
        return travel;
    }

    @Test
    public void testNoOverlap() {
        Travel oldTravel = createTravel(1L, LocalDateTime.now());
        Travel travel = createTravel(2L, LocalDateTime.now());
        User driver = createUser();

         travelService.checkIfTheTravelTimeFrameIsValid(oldTravel, travel, driver);

    }

    @Test
    public void testIsRequestedByUserWhenTravelNotFound() {
        Long travelId = 1L;
        User user = createUser();
        Mockito.when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

        boolean result = travelService.isRequestedByUser(travelId, user);

        assertFalse(result);
    }

    @Test
    public void testIsRequestedByUserWhenUserNotInRequests() {
        Long travelId = 1L;
        User user = createUser();
        Travel travel = createTravel(travelId, Collections.emptyList());
        Mockito.when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

        boolean result = travelService.isRequestedByUser(travelId, user);

        assertFalse(result);
    }

    @Test
    public void testIsRequestedByUserWhenUserInRequests() {
        Long travelId = 1L;
        User user = createUser(2L);
        TravelRequest request = createTravelRequest(user);
        Travel travel = createTravel(travelId, Collections.singletonList(request));
        Mockito.when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

        boolean result = travelService.isRequestedByUser(travelId, user);

        assertTrue(result);
    }

    private Travel createTravel(Long id, List<TravelRequest> requests) {
        Travel travel = new Travel();
        travel.setId(id);
        travel.setTravelRequests(requests);
        return travel;
    }


    private TravelRequest createTravelRequest(User passenger) {
        TravelRequest request = new TravelRequest();
        request.setPassenger(passenger);
        return request;
    }

    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private User createUser() {
        User user = new User();
        return user;
    }

    @Test
    public void testCompleteTravelWithNonExistentId() {
        Long travelId = 1L;
        User editor = createUser(/* editor properties */);

        when(travelRepository.existsById(travelId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            travelService.completeTravel(travelId, editor);
        });
    }

    @Test
    public void testCancelAlreadyStartedTravel() {
        Long travelId = 1L;
        User editor = createUser(/* editor properties */);

        Travel activeTravel = createTravel(travelId, editor, TravelStatus.ACTIVE);

        when(travelRepository.existsById(travelId)).thenReturn(true);
        when(travelRepository.getById(travelId)).thenReturn(activeTravel);

        assertThrows(EntityNotFoundException.class, () -> {
            travelService.cancelTravel(travelId, editor);
        });
    }

    @Test
    public void testCancelTravelWithNonExistentId() {
        Long travelId = 1L;
        User editor = createUser(/* editor properties */);

        when(travelRepository.existsById(travelId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            travelService.cancelTravel(travelId, editor);
        });
    }

    @Test
    public void testCancelTravelWithInvalidEditor() {
        Long travelId = 1L;
        User editor = createUser(/* editor properties */);
        User driver = createUser(/* driver properties */);

        Travel plannedTravel = createTravel(travelId, driver, TravelStatus.PLANNED);

        when(travelRepository.existsById(travelId)).thenReturn(true);
        when(travelRepository.getById(travelId)).thenReturn(plannedTravel);

        assertThrows(EntityNotFoundException.class, () -> {
            travelService.cancelTravel(travelId, editor);
        });
    }

    @Test
    public void testCancelCompletedOrDeletedTravel() {
        Long travelId = 1L;
        User editor = createUser(/* editor properties */);

        Travel deletedTravel = createTravel(travelId, editor, TravelStatus.PLANNED);
        deletedTravel.setDeleted(true);

        when(travelRepository.existsById(travelId)).thenReturn(true);
        when(travelRepository.getById(travelId)).thenReturn(deletedTravel);

        assertThrows(EntityNotFoundException.class, () -> {
            travelService.cancelTravel(travelId, editor);
        });
    }

    private Travel createTravel(Long id, User driver, TravelStatus status) {
        Travel travel = new Travel();
        travel.setId(id);
        travel.setDriver(driver);
        travel.setStatus(status);
        return travel;
    }

    @Test
    public void testCountCompletedTravelsWithSampleData() {
        List<Travel> completedTravels = createSampleCompletedTravels();
        when(travelRepository.countAllByStatusIs(TravelStatus.COMPLETED)).thenReturn((long) completedTravels.size());
        Long count = travelService.countCompleted();
        verify(travelRepository, times(1)).countAllByStatusIs(TravelStatus.COMPLETED);
        assertEquals(completedTravels.size(), count);
    }


    @Test
    public void testCountCompletedTravelsWithNoCompletedTravels() {
        when(travelRepository.findAllByStatusIs(TravelStatus.COMPLETED)).thenReturn(new ArrayList<>());
        Long count = travelService.countCompleted();
        verify(travelRepository, times(1)).countAllByStatusIs(TravelStatus.COMPLETED);
    }

    private List<Travel> createSampleCompletedTravels() {
        List<Travel> travels = new ArrayList<>();
        travels.add(new Travel());
        travels.add(new Travel());

        return travels;
    }

    @Test
    public void testFindByDriverrId() {
        Long driverId = 1L;
        List<Travel> travelsForDriver = createSampleTravelsForDriver(driverId);
        when(travelRepository.findByDriver_Id(driverId)).thenReturn(travelsForDriver);
        List<Travel> result = travelService.findByDriverId(driverId);
        verify(travelRepository, times(1)).findByDriver_Id(driverId);
        assertEquals(travelsForDriver, result);
    }


    @Test
    public void testFindByDriverId() {
        Long driverId = 1L;
        List<Travel> travelsForDriver = createSampleTravelsForDriver(driverId);
        when(travelRepository.findByDriver_Id(driverId)).thenReturn(travelsForDriver);
        List<Travel> result = travelService.findByDriverId(driverId);
        verify(travelRepository, times(1)).findByDriver_Id(driverId);
        assertEquals(travelsForDriver, result);
    }

    private List<Travel> createSampleTravelsAsDriver(User user) {
        List<Travel> travels = new ArrayList<>();
        Travel travel = new Travel();
        Travel travel1 = new Travel();
        travels.add(travel1);
        travels.add(travel);
        return travels;
    }

    private List<Travel> createSampleTravelsForDriver(Long driverId) {
        List<Travel> travels = new ArrayList<>();
        Travel travel = new Travel();
        Travel travel1 = new Travel();
        travels.add(travel1);
        travels.add(travel);
        return travels;
    }

    private List<TravelRequest> createSampleTravelRequestsAsPassenger(User user) {
        List<TravelRequest> travelRequests = new ArrayList<>();
        TravelRequest travelRequest = new TravelRequest();
        TravelRequest travelRequest1 = new TravelRequest();
        travelRequests.add(travelRequest);
        travelRequests.add(travelRequest1);
        return travelRequests;
    }
    public Travel createTravel (Long id , User user , boolean isDeleted) {
        Travel travel = new Travel();
        travel.setId(id);
        travel.setDriver(user);
        travel.setDeleted(isDeleted);
        return travel;
    }

    private List<Travel> createSampleTravelsAsDriverr(User user) {
        return Stream.of(
                createTravel(1L, user, false),
                createTravel(2L, user, true),
                createTravel(3L, user, false)
        ).collect(Collectors.toList());
    }
    @Test
    void testCheckIfTheTravelTimeFrameIsValidd_NoConflicts() {
        Travel travel = new Travel();
        User driver = new User();
        driver.setId(1L);
        when(travelRepository.countConflictingTravels(anyLong(), any(), any())).thenReturn(0);
        travelService.checkIfTheTravelTimeFrameIsValidWithQuery(travel, driver);
    }

    @Test
    void testCheckIfTheTravelTimeFrameIsValid_ConflictsExist() {
        Travel travel = new Travel();
        User driver = new User();
        driver.setId(1L);
        when(travelRepository.countConflictingTravels(anyLong(), any(), any())).thenReturn(1);
        assertThrows(InvalidOperationException.class,
                () -> travelService.checkIfTheTravelTimeFrameIsValidWithQuery(travel, driver));
    }

}