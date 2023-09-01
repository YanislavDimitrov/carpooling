package com.example.carpooling.services;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.exceptions.InvalidOperationException;
import com.example.carpooling.models.Passenger;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.repositories.contracts.PassengerRepository;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.services.contracts.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private UserService userService;

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
        // Mock an empty list of completed travels
        when(travelRepository.getAllByStatusIs(TravelStatus.COMPLETED)).thenReturn(new ArrayList<>());

        // Call the method
        List<Travel> completedTravels = travelService.getAllCompleted();

        // Verify that the returned list is empty
        assertEquals(0, completedTravels.size());
    }

    @Test
    public void testGetAllCompleted_WithCompletedTravels() {
        // Create a list of completed travels
        List<Travel> completedTravels = new ArrayList<>();
        completedTravels.add(new Travel());
        completedTravels.add(new Travel());

        // Mock the repository to return the list of completed travels
        when(travelRepository.getAllByStatusIs(TravelStatus.COMPLETED)).thenReturn(completedTravels);

        // Call the method
        List<Travel> returnedTravels = travelService.getAllCompleted();

        // Verify that the returned list contains the same number of completed travels
        assertEquals(completedTravels.size(), returnedTravels.size());
    }
    @Test
    public void testFindAllByStatusPlanned_NoPlannedTravels() {
        // Mock an empty list of planned travels
        when(travelRepository.getAllByStatusIs(TravelStatus.PLANNED)).thenReturn(new ArrayList<>());

        // Call the method
        List<Travel> plannedTravels = travelService.findAllByStatusPlanned();

        // Verify that the returned list is empty
        assertEquals(0, plannedTravels.size());
    }

    @Test
    public void testFindAllByStatusPlanned_WithPlannedTravels() {
        // Create a list of planned travels
        List<Travel> plannedTravels = new ArrayList<>();
        plannedTravels.add(new Travel());
        plannedTravels.add(new Travel());

        // Mock the repository to return the list of planned travels
        when(travelRepository.getAllByStatusIs(TravelStatus.PLANNED)).thenReturn(plannedTravels);

        // Call the method
        List<Travel> returnedTravels = travelService.findAllByStatusPlanned();

        // Verify that the returned list contains the same number of planned travels
        assertEquals(plannedTravels.size(), returnedTravels.size());
    }
    @Test
    public void testFindAllPlannedPaginated_NoPlannedTravels() {
        // Mock an empty page of planned travels
        when(travelRepository.findAllPlannedPaginated((PageRequest) any(Pageable.class), any(Sort.class), any(Short.class), any(LocalDate.class), any(LocalDate.class), anyString(), anyString(), anyString()))
                .thenReturn(Page.empty());

        // Call the method with sample parameters
        Page<Travel> resultPage = travelService.findAllPlannedPaginated(0, 10, (short) 2, LocalDate.now(), LocalDate.now().plusDays(1), "Start", "End", "10", Sort.by(Sort.Order.asc("departureDate")));

        // Verify that the result page is empty
        assertEquals(0, resultPage.getTotalElements());
    }

    @Test
    public void testFindAllPlannedPaginated_WithPlannedTravels() {
        // Create a list of planned travels
        List<Travel> plannedTravels = new ArrayList<>();
        plannedTravels.add(new Travel());
        plannedTravels.add(new Travel());

        // Create a Page object from the list
        Page<Travel> page = new PageImpl<>(plannedTravels);

        // Mock the repository to return the page of planned travels
        when(travelRepository.findAllPlannedPaginated((PageRequest) any(Pageable.class), any(Sort.class), any(Short.class), any(LocalDate.class), any(LocalDate.class), anyString(), anyString(), anyString()))
                .thenReturn(page);

        // Call the method with sample parameters
        Page<Travel> resultPage = travelService.findAllPlannedPaginated(0, 10, (short) 2, LocalDate.now(), LocalDate.now().plusDays(1), "Start", "End", "10", Sort.by(Sort.Order.asc("departureDate")));

        // Verify that the result page contains the same number of planned travels
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
        // Mock that no passengers are found for the given travel
        Travel travel = new Travel();
        when(passengerRepository.findAllByTravelIs(travel)).thenReturn(new ArrayList<>());

        // Call the method with the mock travel object
        List<User> result = travelService.getAllPassengersForTravel(travel);

        // Verify that the result is an empty list
        assertEquals(0, result.size());
    }
    @Test
    public void testCheckIfTheTravelTimeFrameIsValid_NoConflicts() {


        Travel travel = new Travel();
        User driver = new User();

        // Act and Assert
        assertDoesNotThrow(() -> travelService.checkIfTheTravelTimeFrameIsValid(travel, driver));
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

        // Act and Assert
        assertThrows(InvalidOperationException.class, () -> travelService.checkIfTheTravelTimeFrameIsValid(travel, driver));
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

        // Act and Assert
        assertThrows(InvalidOperationException.class, () -> travelService.checkIfTheTravelTimeFrameIsValid(travel, driver));
    }

    // Add more test cases for other conflict scenarios...

    @Test
    public void testGetAllPassengersForTravel_WithPassengers() {
        // Mock two passengers associated with the given travel
        Travel travel = new Travel();
        Passenger passenger1 = new Passenger();
        Passenger passenger2 = new Passenger();
        List<Passenger> passengers = new ArrayList<>();
        passengers.add(passenger1);
        passengers.add(passenger2);

        when(passengerRepository.findAllByTravelIs(travel)).thenReturn(passengers);

        // Call the method with the mock travel object
        List<User> result = travelService.getAllPassengersForTravel(travel);

        // Verify that the result contains two User objects
        assertEquals(2, result.size());
    }
    @Test
    public void testCount() {
        // Arrange

        // Mock behavior of the travelRepository
        long expectedCount = 10L;
        when(travelRepository.count()).thenReturn(expectedCount);

        // Act
        long actualCount = travelService.count();

        // Assert
        assertEquals(expectedCount, actualCount);
        // Verify that travelRepository.count() was called
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
}