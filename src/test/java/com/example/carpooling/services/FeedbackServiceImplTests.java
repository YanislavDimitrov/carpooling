package com.example.carpooling.services;

import com.example.carpooling.exceptions.*;
import com.example.carpooling.models.*;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.repositories.contracts.FeedbackRepository;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.repositories.contracts.TravelRequestRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.TravelService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FeedbackServiceImplTests {
    private static final long VALID_USER_ID = 1;
    private static final long INVALID_TRAVEL_ID = 999;
    private static final long VALID_TRAVEL_ID = 1;
    private static final long VALID_EDITOR_ID = 2;
    private static final String USER_NOT_FOUND_ERROR = "User with ID %d was not found!";
    private static final String TRAVEL_NOT_FOUND_ERROR = "Travel with ID %d was not found!";
    private static final long VALID_FEEDBACK_ID = 1;
    private static final long VALID_CREATOR_ID = 2;
    private static final long VALID_RECIPIENT_ID = 3;
    private static final long INVALID_USER_ID = 888;
    private static final String TRAVEL_NOT_COMPLETED_ERROR = "Travel with ID %d has not been completed so you cannot leave feedback now.";
    private static final String CANNOT_GIVE_YOURSELF_A_FEEDBACK_ERROR = "You cannot give yourself a feedback!";
    private static final String INVALID_FEEDBACK_ERROR = "You cannot give feedback for this person if he was not part of this travel!";
    private static final String FEEDBACK_REPETITION_ERROR = "You cannot give feedback again for the same person for this travel, if you want to edit your feedback, please go to the edit button!";
    private static final String INVALID_OPERATION_ERROR = "Invalid operation";

    @Mock
    private TravelRequestRepository travelRequestRepository;
    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private TravelRepository travelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TravelService travelService;

    private FeedbackServiceImpl feedbackService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        feedbackService = new FeedbackServiceImpl(feedbackRepository, travelRepository, userRepository, travelService);
    }

    @Test
    void testGetById_ValidId_ReturnsFeedback() {
        long feedbackId = 1;
        Feedback feedback = new Feedback();
        feedback.setId(feedbackId);
        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));
        Feedback result = feedbackService.getById(feedbackId);
        assertEquals(feedback, result);
    }

    @Test
    void testGetById_InvalidId_ThrowsEntityNotFoundException() {
        long invalidId = 100;
        when(feedbackRepository.findById(invalidId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> feedbackService.getById(invalidId));
    }

    @Test
    void testUpdate_ValidFeedback_UpdateFeedback() {
        Feedback originalFeedback = new Feedback();
        Feedback feedbackUpdate = new Feedback();
        User editor = new User();
        originalFeedback.setCreator(editor);
        when(feedbackRepository.existsById(originalFeedback.getId())).thenReturn(true);
        when(feedbackRepository.save(originalFeedback)).thenReturn(originalFeedback);
        Feedback result = feedbackService.update(originalFeedback, feedbackUpdate, editor);
        assertNotNull(result);
        assertEquals(feedbackUpdate.getRating(), originalFeedback.getRating());
        assertEquals(feedbackUpdate.getComment(), originalFeedback.getComment());
        verify(feedbackRepository, times(1)).save(originalFeedback);
    }

    @Test
    void testGet_ReturnsListOfFeedback() {
        Feedback feedback1 = new Feedback();
        Feedback feedback2 = new Feedback();
        List<Feedback> feedbackList = Arrays.asList(feedback1, feedback2);
        when(feedbackRepository.findAll()).thenReturn(feedbackList);
        List<Feedback> result = feedbackService.get();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(feedback1));
        assertTrue(result.contains(feedback2));
        verify(feedbackRepository, times(1)).findAll();
    }

    @Test
    void testGet_EmptyList_ReturnsEmptyList() {
        List<Feedback> emptyList = Arrays.asList();
        when(feedbackRepository.findAll()).thenReturn(emptyList);
        List<Feedback> result = feedbackService.get();
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(feedbackRepository, times(1)).findAll();
    }

    @Test
    void testGetByRecipientIs_ValidUser_ReturnsFeedbackList() {
        User user = new User();
        user.setId(VALID_USER_ID);
        Feedback feedback1 = new Feedback();
        Feedback feedback2 = new Feedback();
        List<Feedback> feedbackList = Arrays.asList(feedback1, feedback2);
        when(userRepository.existsById(VALID_USER_ID)).thenReturn(true);
        when(feedbackRepository.findNonDeletedFeedbacksForRecipient(user)).thenReturn(feedbackList);
        List<Feedback> result = feedbackService.getByRecipientIs(user);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(feedback1));
        assertTrue(result.contains(feedback2));
        verify(userRepository, times(1)).existsById(VALID_USER_ID);
        verify(feedbackRepository, times(1)).findNonDeletedFeedbacksForRecipient(user);
    }

    @Test
    void testGetByRecipientIs_InvalidUser_ThrowsEntityNotFoundException() {
        User invalidUser = new User();
        invalidUser.setId(999L);
        when(userRepository.existsById(invalidUser.getId())).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> feedbackService.getByRecipientIs(invalidUser));
        verify(userRepository, times(1)).existsById(invalidUser.getId());
        verifyNoInteractions(feedbackRepository);
    }

    @Test
    void testFindByCriteria_NoMatchingCriteria_ReturnsEmptyList() {
        Short rating = 4;
        String comment = "Good service";
        Sort sort = Sort.by(Sort.Order.asc("rating"));
        List<Feedback> emptyList = Arrays.asList();
        when(feedbackRepository.findByCriteria(rating, comment, sort)).thenReturn(emptyList);
        List<Feedback> result = feedbackService.findByCriteria(rating, comment, sort);
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(feedbackRepository, times(1)).findByCriteria(rating, comment, sort);
    }

    @Test
    void testFindAllPaginated_ValidCriteria_ReturnsPaginatedFeedbackList() {
        int page = 0;
        int size = 2;
        Sort sort = Sort.by(Sort.Order.asc("rating"));
        Short rating = 4;
        User creator = new User();
        User recipient = new User();
        Travel travel = new Travel();
        List<Feedback> feedbackList = new ArrayList<>();
        feedbackList.add(new Feedback());
        feedbackList.add(new Feedback());
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Feedback> paginatedFeedbackPage = new PageImpl<>(feedbackList, pageRequest, feedbackList.size());
        when(feedbackRepository.findAllPaginated(pageRequest, sort, rating, creator, recipient, travel)).thenReturn(paginatedFeedbackPage);
        Page<Feedback> result = feedbackService.findAllPaginated(page, size, sort, rating, creator, recipient, travel);
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        verify(feedbackRepository, times(1)).findAllPaginated(pageRequest, sort, rating, creator, recipient, travel);
    }

    @Test
    void testFindAllPaginated_NoMatchingCriteria_ReturnsEmptyPage() {
        int page = 0;
        int size = 2;
        Sort sort = Sort.by(Sort.Order.asc("rating"));
        Short rating = 4;
        User creator = new User();
        User recipient = new User();
        Travel travel = new Travel();
        List<Feedback> emptyList = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Feedback> emptyPage = new PageImpl<>(emptyList, pageRequest, emptyList.size());
        when(feedbackRepository.findAllPaginated(pageRequest, sort, rating, creator, recipient, travel)).thenReturn(emptyPage);
        Page<Feedback> result = feedbackService.findAllPaginated(page, size, sort, rating, creator, recipient, travel);
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
        verify(feedbackRepository, times(1)).findAllPaginated(pageRequest, sort, rating, creator, recipient, travel);
    }

    @Test
    void testFindByTravelId_ValidTravelId_ReturnsFeedbackList() {
        long travelId = VALID_TRAVEL_ID;
        Travel validTravel = new Travel();
        validTravel.setId(travelId);
        List<Feedback> feedbackList = new ArrayList<>();
        feedbackList.add(new Feedback());
        feedbackList.add(new Feedback());
        when(travelRepository.existsById(travelId)).thenReturn(true);
        when(feedbackRepository.findByTravelId(travelId)).thenReturn(feedbackList);
        List<Feedback> result = feedbackService.findByTravelId(travelId);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(feedbackList.get(0)));
        assertTrue(result.contains(feedbackList.get(1)));
        verify(travelRepository, times(1)).existsById(travelId);
        verify(feedbackRepository, times(1)).findByTravelId(travelId);
    }

    @Test
    void testFindByTravelId_InvalidTravelId_ThrowsEntityNotFoundException() {
        long invalidTravelId = INVALID_TRAVEL_ID;
        when(travelRepository.existsById(invalidTravelId)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> feedbackService.findByTravelId(invalidTravelId));
        verify(travelRepository, times(1)).existsById(invalidTravelId);
        verifyNoInteractions(feedbackRepository);
    }

    @Test
    void testExistsByTravelAndRecipientAndCreator_Exists_ReturnsTrue() {
        Travel travel = new Travel();
        User recipient = new User();
        User creator = new User();
        when(feedbackRepository.existsByTravelAndRecipientAndCreator(travel, recipient, creator)).thenReturn(true);
        boolean result = feedbackService.existsByTravelAndRecipientAndCreator(travel, recipient, creator);
        assertTrue(result);
        verify(feedbackRepository, times(1)).existsByTravelAndRecipientAndCreator(travel, recipient, creator);
    }

    @Test
    void testExistsByTravelAndRecipientAndCreator_NotExists_ReturnsFalse() {
        Travel travel = new Travel();
        User recipient = new User();
        User creator = new User();
        when(feedbackRepository.existsByTravelAndRecipientAndCreator(travel, recipient, creator)).thenReturn(false);
        boolean result = feedbackService.existsByTravelAndRecipientAndCreator(travel, recipient, creator);
        assertFalse(result);
        verify(feedbackRepository, times(1)).existsByTravelAndRecipientAndCreator(travel, recipient, creator);
    }

    @Test
    void testFindByTravelIsAndCreatorAndRecipient_ValidInput_ReturnsFeedback() {
        // Arrange
        Travel validTravel = new Travel();
        validTravel.setId(VALID_TRAVEL_ID);
        User validCreator = new User();
        validCreator.setId(VALID_CREATOR_ID);
        User validRecipient = new User();
        validRecipient.setId(VALID_RECIPIENT_ID);

        Feedback feedback = new Feedback();

        when(travelRepository.existsById(VALID_TRAVEL_ID)).thenReturn(true);
        when(userRepository.existsById(VALID_CREATOR_ID)).thenReturn(true);
        when(userRepository.existsById(VALID_RECIPIENT_ID)).thenReturn(true);
        when(feedbackRepository.findByTravelIsAndCreatorAndRecipient(validTravel, validCreator, validRecipient)).thenReturn(feedback);

        // Act
        Feedback result = feedbackService.findByTravelIsAndCreatorAndRecipient(validTravel, validCreator, validRecipient);

        // Assert
        assertNotNull(result);
        assertEquals(feedback, result);
        verify(travelRepository, times(1)).existsById(VALID_TRAVEL_ID);
        verify(userRepository, times(1)).existsById(VALID_CREATOR_ID);
        verify(userRepository, times(1)).existsById(VALID_RECIPIENT_ID);
        verify(feedbackRepository, times(1)).findByTravelIsAndCreatorAndRecipient(validTravel, validCreator, validRecipient);
    }

    @Test
    void testFindByTravelIsAndCreatorAndRecipient_InvalidTravel_ThrowsEntityNotFoundException() {
        Travel invalidTravel = new Travel();
        invalidTravel.setId(INVALID_TRAVEL_ID);
        User validCreator = new User();
        validCreator.setId(VALID_CREATOR_ID);
        User validRecipient = new User();
        validRecipient.setId(VALID_RECIPIENT_ID);
        when(travelRepository.existsById(INVALID_TRAVEL_ID)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> feedbackService.findByTravelIsAndCreatorAndRecipient(invalidTravel, validCreator, validRecipient));
        verify(travelRepository, times(1)).existsById(INVALID_TRAVEL_ID);
        verifyNoInteractions(userRepository, feedbackRepository);
    }

    @Test
    void testFindByTravelIsAndCreatorAndRecipient_InvalidUser_ThrowsEntityNotFoundException() {
        Travel validTravel = new Travel();
        validTravel.setId(VALID_TRAVEL_ID);
        User invalidCreator = new User();
        invalidCreator.setId(INVALID_USER_ID);
        User validRecipient = new User();
        validRecipient.setId(VALID_RECIPIENT_ID);
        when(travelRepository.existsById(VALID_TRAVEL_ID)).thenReturn(true);
        when(userRepository.existsById(INVALID_USER_ID)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> feedbackService.findByTravelIsAndCreatorAndRecipient(validTravel, invalidCreator, validRecipient));
        verify(travelRepository, times(1)).existsById(VALID_TRAVEL_ID);
        verify(userRepository, times(1)).existsById(INVALID_USER_ID);
        verifyNoInteractions(feedbackRepository);
    }

    @Test
    void testFindAll_ValidSort_ReturnsFeedbackList() {
        Sort sort = Sort.by(Sort.Order.asc("rating"));
        List<Feedback> feedbackList = new ArrayList<>();
        feedbackList.add(new Feedback());
        feedbackList.add(new Feedback());
        when(feedbackRepository.findAll()).thenReturn(feedbackList);
        List<Feedback> result = feedbackService.findAll(sort);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(feedbackList.get(0)));
        assertTrue(result.contains(feedbackList.get(1)));
        verify(feedbackRepository, times(1)).findAll();
    }

    @Test
    void testCount_ReturnsFeedbackCount() {
        long feedbackCount = 5L;
        when(feedbackRepository.count()).thenReturn(feedbackCount);
        Long result = feedbackService.count();
        assertNotNull(result);
        assertEquals(feedbackCount, result);
        verify(feedbackRepository, times(1)).count();
    }

    @Test
    void testCreatee_TravelNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Travel invalidTravel = new Travel();
        invalidTravel.setId(VALID_TRAVEL_ID);
        User validCreator = new User();
        validCreator.setId(VALID_CREATOR_ID);
        User validRecipient = new User();
        validRecipient.setId(VALID_RECIPIENT_ID);
        Feedback feedback = new Feedback();

        when(travelRepository.existsById(VALID_TRAVEL_ID)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> feedbackService.create(invalidTravel, validCreator, validRecipient, feedback));
        verify(travelRepository, times(1)).existsById(VALID_TRAVEL_ID);
        verifyNoInteractions(userRepository, feedbackRepository);
    }

    @Test
    void testCreatee_UserNotFound_ThrowsEntityNotFoundException() {
        Travel validTravel = new Travel();
        validTravel.setId(VALID_TRAVEL_ID);
        User invalidRecipient = new User();
        invalidRecipient.setId(VALID_RECIPIENT_ID);
        User validCreator = new User();
        validCreator.setId(VALID_CREATOR_ID);
        Feedback feedback = new Feedback();
        when(travelRepository.existsById(VALID_TRAVEL_ID)).thenReturn(true);
        when(userRepository.existsById(VALID_RECIPIENT_ID)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> feedbackService.create(validTravel, validCreator, invalidRecipient, feedback));
        verify(travelRepository, times(1)).existsById(VALID_TRAVEL_ID);
        verify(userRepository, times(1)).existsById(VALID_RECIPIENT_ID);
        verifyNoInteractions(feedbackRepository);
    }

    @Test
    void testCreate_TravelNotFound_ThrowsEntityNotFoundException() {
        Travel invalidTravel = new Travel();
        invalidTravel.setId(VALID_TRAVEL_ID);
        User validCreator = new User();
        validCreator.setId(VALID_CREATOR_ID);
        User validRecipient = new User();
        validRecipient.setId(VALID_RECIPIENT_ID);
        Feedback feedback = new Feedback();
        when(travelRepository.existsById(VALID_TRAVEL_ID)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> feedbackService.create(invalidTravel, validCreator, validRecipient, feedback));
        verify(travelRepository, times(1)).existsById(VALID_TRAVEL_ID);
        verifyNoInteractions(userRepository, feedbackRepository);
    }

    @Test
    void testCreate_UserNotFound_ThrowsEntityNotFoundException() {
        Travel validTravel = new Travel();
        validTravel.setId(VALID_TRAVEL_ID);
        User invalidRecipient = new User();
        invalidRecipient.setId(VALID_RECIPIENT_ID);
        User validCreator = new User();
        validCreator.setId(VALID_CREATOR_ID);
        Feedback feedback = new Feedback();
        when(travelRepository.existsById(VALID_TRAVEL_ID)).thenReturn(true);
        when(userRepository.existsById(VALID_RECIPIENT_ID)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> feedbackService.create(validTravel, validCreator, invalidRecipient, feedback));
        verify(travelRepository, times(1)).existsById(VALID_TRAVEL_ID);
        verify(userRepository, times(1)).existsById(VALID_RECIPIENT_ID);
        verifyNoInteractions(feedbackRepository);
    }

    @Test
    void testCreate_TravelNotCompleted_ThrowsTravelNotCompletedException() {
        Travel validTravel = new Travel();
        validTravel.setId(VALID_TRAVEL_ID);
        validTravel.setStatus(TravelStatus.PLANNED);
        User validCreator = new User();
        validCreator.setId(VALID_CREATOR_ID);
        User validRecipient = new User();
        validRecipient.setId(VALID_RECIPIENT_ID);
        Feedback feedback = new Feedback();
        when(travelRepository.existsById(VALID_TRAVEL_ID)).thenReturn(true);
        when(userRepository.existsById(VALID_RECIPIENT_ID)).thenReturn(true);
        assertThrows(TravelNotCompletedException.class, () -> feedbackService.create(validTravel, validCreator, validRecipient, feedback));
        verify(travelRepository, times(1)).existsById(VALID_TRAVEL_ID);
        verify(userRepository, times(1)).existsById(VALID_RECIPIENT_ID);
        verifyNoInteractions(feedbackRepository);
    }

    @Test
    void testExistsByTravelAndCreator_ValidInput_ReturnsTrue() {
        Travel validTravel = new Travel();
        validTravel.setId(VALID_TRAVEL_ID);
        User validCreator = new User();
        validCreator.setId(VALID_CREATOR_ID);
        when(travelRepository.existsById(VALID_TRAVEL_ID)).thenReturn(true);
        when(userRepository.existsById(VALID_CREATOR_ID)).thenReturn(true);
        when(feedbackRepository.existsByTravelAndCreator(validTravel, validCreator)).thenReturn(true);
        boolean result = feedbackService.existsByTravelAndCreator(validTravel, validCreator);
        assertTrue(result);
        verify(travelRepository, times(1)).existsById(VALID_TRAVEL_ID);
        verify(userRepository, times(1)).existsById(VALID_CREATOR_ID);
        verify(feedbackRepository, times(1)).existsByTravelAndCreator(validTravel, validCreator);
    }

    @Test
    void testExistsByTravelAndCreator_TravelNotFound_ThrowsEntityNotFoundException() {
        Travel invalidTravel = new Travel();
        invalidTravel.setId(VALID_TRAVEL_ID);
        User validCreator = new User();
        validCreator.setId(VALID_CREATOR_ID);
        when(travelRepository.existsById(VALID_TRAVEL_ID)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> feedbackService.existsByTravelAndCreator(invalidTravel, validCreator));
        verify(travelRepository, times(1)).existsById(VALID_TRAVEL_ID);
        verifyNoInteractions(userRepository, feedbackRepository);
    }

    @Test
    void testExistsByTravelAndCreator_UserNotFound_ThrowsEntityNotFoundException() {
        Travel validTravel = new Travel();
        validTravel.setId(VALID_TRAVEL_ID);
        User invalidCreator = new User();
        invalidCreator.setId(VALID_CREATOR_ID);
        when(travelRepository.existsById(VALID_TRAVEL_ID)).thenReturn(true);
        when(userRepository.existsById(VALID_CREATOR_ID)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> feedbackService.existsByTravelAndCreator(validTravel, invalidCreator));
        verify(travelRepository, times(1)).existsById(VALID_TRAVEL_ID);
        verify(userRepository, times(1)).existsById(VALID_CREATOR_ID);
        verifyNoInteractions(feedbackRepository);
    }

    @Test
    void testExistsByTravelAndCreator_FeedbackDoesNotExist_ReturnsFalse() {
        Travel validTravel = new Travel();
        validTravel.setId(VALID_TRAVEL_ID);
        User validCreator = new User();
        validCreator.setId(VALID_CREATOR_ID);
        when(travelRepository.existsById(VALID_TRAVEL_ID)).thenReturn(true);
        when(userRepository.existsById(VALID_CREATOR_ID)).thenReturn(true);
        when(feedbackRepository.existsByTravelAndCreator(validTravel, validCreator)).thenReturn(false);
        boolean result = feedbackService.existsByTravelAndCreator(validTravel, validCreator);
        assertFalse(result);
        verify(travelRepository, times(1)).existsById(VALID_TRAVEL_ID);
        verify(userRepository, times(1)).existsById(VALID_CREATOR_ID);
        verify(feedbackRepository, times(1)).existsByTravelAndCreator(validTravel, validCreator);
    }

    @Test
    void testUpdate_FeedbackNotFound_ThrowsEntityNotFoundException() {
        Feedback originalFeedback = new Feedback();
        originalFeedback.setId(VALID_FEEDBACK_ID);
        User editor = new User();
        editor.setId(VALID_EDITOR_ID);
        Feedback feedbackUpdate = new Feedback();
        when(feedbackRepository.existsById(VALID_FEEDBACK_ID)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> feedbackService.update(originalFeedback, feedbackUpdate, editor));
        verify(feedbackRepository, times(1)).existsById(VALID_FEEDBACK_ID);
        verifyNoMoreInteractions(feedbackRepository);
    }

    @Test
    void testUpdate_NotAuthorized_ThrowsAuthorizationException() {
        Feedback originalFeedback = new Feedback();
        originalFeedback.setId(VALID_FEEDBACK_ID);
        User editor = new User();
        editor.setId(VALID_EDITOR_ID);
        originalFeedback.setCreator(new User());
        Feedback feedbackUpdate = new Feedback();
        when(feedbackRepository.existsById(VALID_FEEDBACK_ID)).thenReturn(true);
        assertThrows(AuthorizationException.class, () -> feedbackService.update(originalFeedback, feedbackUpdate, editor));
        verify(feedbackRepository, times(1)).existsById(VALID_FEEDBACK_ID);
        verifyNoMoreInteractions(feedbackRepository);
    }


    @Test
    @Transactional(propagation = Propagation.REQUIRED)
    void testDelete_FeedbackNotFound_ThrowsEntityNotFoundException() {
        User editor = new User();
        editor.setId(VALID_EDITOR_ID);
        when(feedbackRepository.existsById(VALID_FEEDBACK_ID)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> feedbackService.delete(VALID_FEEDBACK_ID, editor));
        verify(feedbackRepository, times(1)).existsById(VALID_FEEDBACK_ID);
        verifyNoMoreInteractions(feedbackRepository);
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED)
    void testDelete_NotAuthorized_ThrowsAuthorizationException() {
        Feedback feedback = new Feedback();
        feedback.setId(VALID_FEEDBACK_ID);
        feedback.setCreator(new User());
        User editor = new User();
        editor.setId(VALID_EDITOR_ID);
        feedback.setCreator(new User()); // Set a different creator than the editor
        when(feedbackRepository.existsById(VALID_FEEDBACK_ID)).thenReturn(true);
        when(feedbackRepository.findById(VALID_FEEDBACK_ID)).thenReturn(java.util.Optional.of(feedback));
        assertThrows(AuthorizationException.class, () -> feedbackService.delete(VALID_FEEDBACK_ID, editor));
        verify(feedbackRepository, times(1)).existsById(VALID_FEEDBACK_ID);
        verify(feedbackRepository, times(1)).findById(VALID_FEEDBACK_ID);
        verifyNoMoreInteractions(feedbackRepository);
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED)
    void testDelete_ValidInput_DeletesFeedback() {
        Feedback feedback = new Feedback();
        feedback.setId(VALID_FEEDBACK_ID);
        User creatorAndEditor = new User();
        creatorAndEditor.setId(VALID_CREATOR_ID);
        feedback.setCreator(creatorAndEditor);
        when(feedbackRepository.existsById(VALID_FEEDBACK_ID)).thenReturn(true);
        when(feedbackRepository.findById(VALID_FEEDBACK_ID)).thenReturn(Optional.of(feedback));
        assertDoesNotThrow(() -> feedbackService.delete(VALID_FEEDBACK_ID, creatorAndEditor));
        verify(feedbackRepository, times(1)).existsById(VALID_FEEDBACK_ID);
        verify(feedbackRepository, times(1)).findById(VALID_FEEDBACK_ID);
        verify(feedbackRepository, times(1)).delete(VALID_FEEDBACK_ID);
    }

    @Test
    public void testEntitiesExist() {
        Travel travel = new Travel();
        User creator = new User();
        User recipient = new User();
        when(travelRepository.existsById(travel.getId())).thenReturn(true);
        when(userRepository.existsById(creator.getId())).thenReturn(true);
        when(userRepository.existsById(recipient.getId())).thenReturn(true);
        assertDoesNotThrow(() -> feedbackService.checkIfTravelAndUsersExist(
                travel, creator, recipient, travelRepository, "Travel not found", userRepository, "User not found"
        ));
    }

    @Test
    public void testTravelNotFound() {
        Travel travel = new Travel();
        User creator = new User();
        User recipient = new User();
        when(travelRepository.existsById(travel.getId())).thenReturn(false);
        when(userRepository.existsById(creator.getId())).thenReturn(true);
        when(userRepository.existsById(recipient.getId())).thenReturn(true);
        assertThrows(EntityNotFoundException.class, () -> feedbackService.checkIfTravelAndUsersExist(
                travel, creator, recipient, travelRepository, "Travel not found", userRepository, "User not found"
        ));
    }

    @Test
    public void testRecipientUserNotFound() {
        Travel travel = new Travel();
        User creator = new User();
        User recipient = new User();
        when(travelRepository.existsById(travel.getId())).thenReturn(true);
        when(userRepository.existsById(creator.getId())).thenReturn(true);
        when(userRepository.existsById(recipient.getId())).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> feedbackService.checkIfTravelAndUsersExist(
                travel, creator, recipient, travelRepository, "Travel not found", userRepository, "User not found"
        ));
    }

    public TravelRequest createRequest(Travel travel, User recipient, TravelRequestStatus travelRequestStatus) {
        TravelRequest travelRequest = new TravelRequest();
        travelRequest.setTravel(travel);
        travelRequest.setPassenger(recipient);
        travelRequest.setStatus(travelRequestStatus);
        return travelRequest;
    }
    @Test
    public void testHaveTravelledTogetherWhenDriverAndRecipientMatch() {

        Long travelId = 1L;
        User driver = new User();
        User recipient = new User();
        Travel travel = new Travel();
        travel.setDriver(driver);
        when(travelService.getById(travelId)).thenReturn(travel);


        TravelRequest travelRequest = new TravelRequest();
        travelRequest.setStatus(TravelRequestStatus.APPROVED);
        travelRequest.setTravel(travel);
        travelRequest.setPassenger(recipient);


        boolean result = feedbackService.haveTravelledTogether(travelId, driver, recipient);


        assertFalse(result);
    }
    @Test
    public void testHaveTravelledTogetherWhenDriverIsRecipient() {
        Long travelId = 1L;
        User driver = new User();
        driver.setId(1L);
        Travel travel = new Travel();
        travel.setDriver(driver);
        when(travelService.getById(travelId)).thenReturn(travel);

        TravelRequest driverTravelRequest = new TravelRequest();
        driverTravelRequest.setTravel(travel);
        driverTravelRequest.setStatus(TravelRequestStatus.APPROVED);
        driver.getTravelsAsPassenger().add(driverTravelRequest);

        boolean result = feedbackService.haveTravelledTogether(travelId, driver, driver);

        assertTrue(result);
    }
    @Test
    public void testHaveTravelledTogetherWhenDriverAndRecipientAreDifferent() {

        Long travelId = 1L;
        User driver = new User();
        User recipient = new User();
        Travel travel = new Travel();
        travel.setDriver(driver);
        when(travelService.getById(travelId)).thenReturn(travel);


        TravelRequest recipientTravelRequest = new TravelRequest();
        recipientTravelRequest.setTravel(travel);
        recipientTravelRequest.setStatus(TravelRequestStatus.APPROVED);
        recipient.getTravelsAsPassenger().add(recipientTravelRequest);


        boolean result = feedbackService.haveTravelledTogether(travelId, driver, recipient);


        assertTrue(result);
    }
    @Test
    public void testHaveTravelledTogetherWhenRecipientIsDriverAndHasApprovedTravelRequest() {

        Long travelId = 1L;
        User driver = new User();
        User recipient = new User();
        Travel travel = new Travel();
        travel.setDriver(recipient);
        when(travelService.getById(travelId)).thenReturn(travel);

        TravelRequest driverTravelRequest = new TravelRequest();
        driverTravelRequest.setTravel(travel);
        driverTravelRequest.setStatus(TravelRequestStatus.APPROVED);
        recipient.getTravelsAsPassenger().add(driverTravelRequest);


        boolean result = feedbackService.haveTravelledTogether(travelId, driver, recipient);

        assertTrue(result);
    }
    @Test
    public void testHaveTravelledTogetherWhenRecipientIsDriverButNoApprovedTravelRequest() {
        // Arrange
        Long travelId = 1L;
        User driver = new User();
        User recipient = new User();
        Travel travel = new Travel();
        travel.setDriver(recipient);
        when(travelService.getById(travelId)).thenReturn(travel);


        TravelRequest driverTravelRequest = new TravelRequest();
        driverTravelRequest.setTravel(travel);
        driverTravelRequest.setStatus(TravelRequestStatus.PENDING);
        recipient.getTravelsAsPassenger().add(driverTravelRequest);


        boolean result = feedbackService.haveTravelledTogether(travelId, driver, recipient);


        assertFalse(result);
    }
    @Test
    public void testHaveTravelledTogetherWhenRecipientIsDriverAndHasApprovedTravelRequestAsPassenger() {

        Long travelId = 1L;
        User driver = new User();
        User recipient = new User();
        Travel travel = new Travel();
        travel.setDriver(recipient); // Set the driver of the travel to be the recipient
        when(travelService.getById(travelId)).thenReturn(travel);


        TravelRequest driverTravelRequest = new TravelRequest();
        driverTravelRequest.setTravel(travel);
        driverTravelRequest.setStatus(TravelRequestStatus.APPROVED);
        recipient.getTravelsAsPassenger().add(driverTravelRequest);
        Passenger passenger = new Passenger();
        passenger.setTravel(travel);
        passenger.setUser(driver);
        passenger.setActive(true);

        boolean result = feedbackService.haveTravelledTogether(travelId, driver, recipient);


        assertTrue(result);
    }
    @Test
    public void testCreateValidFeedback() {
        Travel travel = new Travel();
        travel.setId(1L);
        User creator = new User();
        creator.setId(2L);
        User recipient = new User();
        recipient.setId(3L);
        Feedback feedback = new Feedback();
        when(travelRepository.existsById(travel.getId())).thenReturn(true);
        when(userRepository.existsById(recipient.getId())).thenReturn(true);
        when(travelRepository.findById(travel.getId())).thenReturn(Optional.of(travel));
        when(feedbackRepository.save(feedback)).thenReturn(feedback);
      Assertions.assertThrows(TravelNotCompletedException.class,()->feedbackService.create(travel,creator,recipient,feedback));
    }
    @Test
    public void testCreateFeedbackWithNonExistentTravel() {
        Travel travel = new Travel();
        travel.setId(1L);
        User creator = new User();
        User recipient = new User();
        Feedback feedback = new Feedback();
        when(travelRepository.existsById(travel.getId())).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () ->
                feedbackService.create(travel, creator, recipient, feedback));
    }
    @Test
    public void testCreateFeedbackWithNonExistentRecipient() {

        Travel travel = new Travel();
        travel.setId(1L);
        User creator = new User();
        creator.setId(2L);
        User recipient = new User();
        recipient.setId(3L);
        Feedback feedback = new Feedback();
        when(travelRepository.existsById(travel.getId())).thenReturn(true);
        when(userRepository.existsById(recipient.getId())).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () ->
                feedbackService.create(travel, creator, recipient, feedback));
    }
    @Test
    public void testCreateFeedbackWithIncompleteTravel() {
        // Arrange
        Travel travel = new Travel();
        travel.setId(1L);
        travel.setStatus(TravelStatus.PLANNED); // Not completed
        User creator = new User();
        User recipient = new User();
        Feedback feedback = new Feedback();
        when(travelRepository.existsById(travel.getId())).thenReturn(true);
        when(userRepository.existsById(recipient.getId())).thenReturn(true);
        when(travelRepository.findById(travel.getId())).thenReturn(Optional.of(travel));

        assertThrows(TravelNotCompletedException.class, () ->
                feedbackService.create(travel, creator, recipient, feedback));
    }
    @Test
    public void testCreateFeedbackForSameCreatorAndRecipient() {

        Travel travel = new Travel();
        travel.setId(1L);
        User creator = new User();
        creator.setId(2L);
        User recipient = creator; // Same user
        Feedback feedback = new Feedback();

        assertThrows(EntityNotFoundException.class, () ->
                feedbackService.create(travel, creator, recipient, feedback));
    }
    @Test
    public void testCreateValidFeedbackForExistingFeedback() {

        Travel travel = new Travel();
        travel.setId(1L);
        travel.setStatus(TravelStatus.COMPLETED);
        User creator = new User();
        creator.setId(2L);
        User recipient = new User();
        recipient.setId(3L);
        Feedback feedback = new Feedback();
        Feedback existingFeedback = new Feedback();
        existingFeedback.setDeleted(true);
        when(travelRepository.existsById(travel.getId())).thenReturn(true);
        when(userRepository.existsById(recipient.getId())).thenReturn(true);
        when(travelRepository.findById(travel.getId())).thenReturn(Optional.of(travel));
        when(feedbackRepository.save(feedback)).thenReturn(feedback);
        when(feedbackRepository.findByTravelIsAndCreatorAndRecipient(travel, creator, recipient))
                .thenReturn(existingFeedback);

Assertions.assertThrows(InvalidFeedbackException.class,()->feedbackService.create(travel,creator,recipient,feedback));

    }
    @Test
    public void testCreateFeedbackForExistingDeletedFeedback() {
        Travel travel = new Travel();
        travel.setId(1L);
        travel.setStatus(TravelStatus.COMPLETED);
        User creator = new User();
        creator.setId(2L);
        User recipient = new User();
        recipient.setId(3L);
        Feedback feedback = new Feedback();
        Feedback existingFeedback = new Feedback();
        existingFeedback.setDeleted(true); // Deleted feedback
        when(travelRepository.existsById(travel.getId())).thenReturn(true);
        when(userRepository.existsById(recipient.getId())).thenReturn(true);
        when(travelRepository.findById(travel.getId())).thenReturn(Optional.of(travel));
        when(feedbackRepository.findByTravelIsAndCreatorAndRecipient(travel, creator, recipient))
                .thenReturn(existingFeedback);
        assertThrows(InvalidFeedbackException.class, () ->
                feedbackService.create(travel, creator, recipient, feedback));
    }

}
