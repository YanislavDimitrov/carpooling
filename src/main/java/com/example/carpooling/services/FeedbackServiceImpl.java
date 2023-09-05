package com.example.carpooling.services;

import com.example.carpooling.exceptions.*;
import com.example.carpooling.models.Feedback;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.repositories.contracts.FeedbackRepository;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.FeedbackService;
import com.example.carpooling.services.contracts.TravelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The {@code FeedbackServiceImpl} class provides implementation for managing feedback related operations.
 * It allows creating, updating, deleting, and retrieving feedback objects, as well as various query operations.
 * Feedback is associated with specific travels and users.
 *
 * @author Ivan Boev
 * @version 1.0
 * @since 2023-09-04
 */
@Service
public class FeedbackServiceImpl implements FeedbackService {
    // Constant messages for errors
    public static final String FEEDBACK_NOT_FOUND = "Feedback with ID %d was not found!";
    public static final String TRAVEL_NOT_FOUND = "Travel with ID %d was not found!";
    public static final String USER_NOT_FOUND = "User with ID %d was not found";
    public static final String TRAVEL_NOT_COMPLETED = "Travel with ID %d has not been completed so you cannot leave feedback now.";
    public static final String NOT_AUTHORIZED = "You are not authorized to update feedback because only creators of the feedback can update it!";
    public static final String INVALID_FEEDBACK = "You cannot give feedback for this person if he was not part of this travel!";
    public static final String FEEDBACK_REPETITION = "You cannot give feedback again for the same person for this travel,if you want to edit your feedback , please go to the edit button!";
    public static final String CANNOT_GIVE_YOURSELF_A_FEEDBACK = "You cannot give yourself a feedback!";

    // Injected dependencies
    private final FeedbackRepository feedbackRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    private final TravelService travelService;

    @Autowired
    public FeedbackServiceImpl(FeedbackRepository feedbackRepository, TravelRepository travelRepository, UserRepository userRepository, TravelService travelService) {
        this.feedbackRepository = feedbackRepository;
        this.travelRepository = travelRepository;
        this.userRepository = userRepository;
        this.travelService = travelService;
    }

    /**
     * Retrieves a list of all feedbacks.
     *
     * @return A list of all feedbacks.
     */
    @Override
    public List<Feedback> get() {
        return feedbackRepository.findAll();
    }

    /**
     * Retrieves a feedback by its ID.
     *
     * @param id The ID of the feedback to retrieve.
     * @return The feedback with the specified ID.
     * @throws EntityNotFoundException If the feedback with the given ID does not exist.
     */
    @Override
    public Feedback getById(Long id) {
        return feedbackRepository
                .findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException(String.format(FEEDBACK_NOT_FOUND, id))
                );
    }

    /**
     * Retrieves a list of feedbacks for a specific user.
     *
     * @param user The recipient user for whom feedbacks are retrieved.
     * @return A list of feedbacks received by the specified user.
     * @throws EntityNotFoundException If the specified user does not exist.
     */
    @Override
    public List<Feedback> getByRecipientIs(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND, user.getId()));
        }
        return feedbackRepository.findNonDeletedFeedbacksForRecipient(user);
    }

    /**
     * Retrieves a list of feedbacks based on specified criteria.
     *
     * @param rating  The rating to filter by (can be null for no filtering).
     * @param comment The comment to filter by (can be null for no filtering).
     * @param sort    The sorting order for the results.
     * @return A list of feedbacks that match the criteria.
     */
    @Override
    public List<Feedback> findByCriteria(Short rating, String comment, Sort sort) {
        return feedbackRepository.findByCriteria(rating, comment, sort);
    }

    /**
     * Retrieves a paginated list of feedbacks based on various parameters.
     *
     * @param page      The page number of the result set (0-based index).
     * @param size      The maximum number of feedbacks to retrieve per page.
     * @param sort      The sorting order for the feedbacks.
     * @param rating    The rating to filter by (can be null for no filtering).
     * @param creator   The creator of the feedback (can be null for no filtering).
     * @param recipient The recipient of the feedback (can be null for no filtering).
     * @param travel    The associated travel for the feedback (can be null for no filtering).
     * @return A paginated list of feedbacks that match the specified criteria.
     */
    @Override
    public Page<Feedback> findAllPaginated(int page, int size, Sort sort, Short rating, User creator, User recipient, Travel travel) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return feedbackRepository.findAllPaginated(pageRequest, sort, rating, creator, recipient, travel);
    }

    /**
     * Retrieves a list of feedbacks associated with a specific travel.
     *
     * @param id The ID of the travel for which feedbacks are retrieved.
     * @return A list of feedbacks associated with the specified travel.
     * @throws EntityNotFoundException If the travel with the given ID does not exist.
     */
    @Override
    public List<Feedback> findByTravelId(Long id) {
        if (!travelRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, id));
        }
        return feedbackRepository.findByTravelId(id);
    }

    /**
     * Checks if a feedback exists for a specific travel, recipient, and creator combination.
     *
     * @param travel    The travel associated with the feedback.
     * @param recipient The recipient of the feedback.
     * @param creator   The creator of the feedback.
     * @return true if a feedback exists for the specified combination, otherwise false.
     */
    @Override
    public boolean existsByTravelAndRecipientAndCreator(Travel travel, User recipient, User creator) {
        return feedbackRepository.existsByTravelAndRecipientAndCreator(travel, recipient, creator);
    }

    /**
     * Finds and retrieves a feedback associated with a specific travel, creator, and recipient.
     *
     * @param travel    The travel associated with the feedback.
     * @param creator   The creator of the feedback.
     * @param recipient The recipient of the feedback.
     * @return The feedback associated with the specified travel, creator, and recipient.
     * @throws EntityNotFoundException If the travel with the given ID or the specified creator or recipient do not exist.
     */
    @Override
    public Feedback findByTravelIsAndCreatorAndRecipient(Travel travel, User creator, User recipient) {
        if (!travelRepository.existsById(travel.getId())) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, travel.getId()));
        }
        if (!userRepository.existsById(creator.getId()) || !userRepository.existsById(recipient.getId())) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND, creator.getId()));
        }
        return feedbackRepository.findByTravelIsAndCreatorAndRecipient(travel, creator, recipient);
    }

    /**
     * Retrieves a list of all feedbacks sorted according to the provided sorting order.
     *
     * @param sort The sorting order for the feedbacks.
     * @return A list of all feedbacks sorted as specified.
     */
    @Override
    public List<Feedback> findAll(Sort sort) {
        return feedbackRepository.findAll();
    }

    /**
     * Counts the total number of feedbacks in the system.
     *
     * @return The total number of feedbacks.
     */
    @Override
    public Long count() {
        return feedbackRepository.count();
    }

    /**
     * Creates a new feedback for a travel, specifying the creator, recipient, and feedback details.
     *
     * @param travel    The travel associated with the feedback.
     * @param creator   The creator of the feedback.
     * @param recipient The recipient of the feedback.
     * @param feedback  The feedback to be created.
     * @return The newly created feedback.
     * @throws EntityNotFoundException     If the travel or recipient with the given IDs do not exist.
     * @throws TravelNotCompletedException If the associated travel is not marked as completed.
     * @throws InvalidFeedbackException    If the creator tries to give feedback to themselves,
     *                                     or if feedback for the same person on the same travel already exists and is not deleted.
     * @throws InvalidOperationException   If an attempt is made to give feedback again for the same person on the same travel.
     */
    @Override
    public Feedback create(Travel travel, User creator, User recipient, Feedback feedback) {
        if (!travelRepository.existsById(travel.getId())) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, travel.getId()));
        }
        if (!userRepository.existsById(recipient.getId())) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND, recipient.getId()));
        }
        if (travel.getStatus() != TravelStatus.COMPLETED) {
            throw new TravelNotCompletedException(String.format(TRAVEL_NOT_COMPLETED, travel.getId()));
        }
        if (creator.equals(recipient)) {
            throw new InvalidFeedbackException(CANNOT_GIVE_YOURSELF_A_FEEDBACK);
        }
        Feedback feedbackToCheck;
        if (!existsByTravelAndRecipientAndCreator(travel, recipient, creator)
                && haveTravelledTogether(travel.getId(), creator, recipient)) {
            feedback.setCreator(creator);
            feedback.setRecipient(recipient);
            feedback.setTravel(travel);
            feedbackRepository.save(feedback);
            return feedback;
        }
        if (existsByTravelAndRecipientAndCreator(travel, recipient, creator)) {
            feedbackToCheck = findByTravelIsAndCreatorAndRecipient(travel, creator, recipient);
            if (feedbackToCheck.isDeleted() && haveTravelledTogether(travel.getId(), creator, recipient)) {
                feedback.setCreator(creator);
                feedback.setRecipient(recipient);
                feedback.setTravel(travel);
                feedbackRepository.save(feedback);
                return feedback;
            }
            throw new InvalidFeedbackException(INVALID_FEEDBACK);
        }
        throw new InvalidOperationException(FEEDBACK_REPETITION);
    }

    /**
     * Checks if feedback exists for a specific travel and creator combination.
     *
     * @param travel  The travel associated with the feedback.
     * @param creator The creator of the feedback.
     * @return true if feedback exists for the specified combination, otherwise false.
     * @throws EntityNotFoundException If the travel or creator with the given IDs do not exist.
     */
    @Override
    public boolean existsByTravelAndCreator(Travel travel, User creator) {
        if (!travelRepository.existsById(travel.getId())) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, travel.getId()));
        }
        if (!userRepository.existsById(creator.getId())) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND, creator.getId()));
        }
        return feedbackRepository.existsByTravelAndCreator(travel, creator);
    }

    /**
     * Updates an existing feedback with new rating and comment values.
     *
     * @param originalFeedback The original feedback to be updated.
     * @param feedbackUpdate   The updated feedback containing new rating and comment values.
     * @param editor           The user attempting to update the feedback.
     * @return The updated feedback after the changes.
     * @throws EntityNotFoundException If the original feedback does not exist.
     * @throws AuthorizationException  If the editor is not authorized to update the feedback.
     */
    @Override
    public Feedback update(Feedback originalFeedback, Feedback feedbackUpdate, User editor) {
        if (!feedbackRepository.existsById(originalFeedback.getId())) {
            throw new EntityNotFoundException(String.format(FEEDBACK_NOT_FOUND, originalFeedback.getId()));
        }
        if (originalFeedback.getCreator() != editor) {
            throw new AuthorizationException(NOT_AUTHORIZED);
        }
        originalFeedback.setRating(feedbackUpdate.getRating());
        originalFeedback.setComment(feedbackUpdate.getComment());
        feedbackRepository.save(originalFeedback);
        return originalFeedback;
    }

    /**
     * Deletes a feedback with the specified ID if the editor is authorized to do so.
     *
     * @param id     The ID of the feedback to be deleted.
     * @param editor The user attempting to delete the feedback.
     * @throws EntityNotFoundException If the feedback with the given ID does not exist.
     * @throws AuthorizationException  If the editor is not authorized to delete the feedback.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long id, User editor) {
        if (!feedbackRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format(FEEDBACK_NOT_FOUND, id));
        }
        Feedback feedback = getById(id);
        if (feedback.getCreator() != editor) {
            throw new AuthorizationException(NOT_AUTHORIZED);
        }
        feedbackRepository.delete(id);
    }

    /**
     * Checks whether two users, a driver, and a recipient have traveled together on a specific travel.
     *
     * @param travelId  The ID of the travel to check.
     * @param driver    The driver user.
     * @param recipient The recipient user.
     * @return true if the driver and recipient have traveled together on the specified travel; false otherwise.
     */
    public boolean haveTravelledTogether(Long travelId, User driver, User recipient) {
        Travel travel = travelService.getById(travelId);
        if (travel.getDriver().equals(driver)) {
            for (TravelRequest travelToCheck : recipient.getTravelsAsPassenger()) {
                if (travelToCheck.getTravel().equals(travel) && travelToCheck.getStatus() == TravelRequestStatus.APPROVED) {
                    return true;
                }
            }
        } else if (travel.getDriver().equals(recipient)) {
            for (TravelRequest travelRequest : driver.getTravelsAsPassenger()) {
                if (travelRequest.getTravel().equals(travel) && travelRequest.getStatus() == TravelRequestStatus.APPROVED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Static utility method to check if a travel and users exist by their IDs.
     *
     * @param travel           The travel to check.
     * @param creator          The creator user.
     * @param recipient        The recipient user.
     * @param travelRepository The repository for travel operations.
     * @param travelNotFound   The error message for a non-existent travel.
     * @param userRepository   The repository for user operations.
     * @param userNotFound     The error message for a non-existent user.
     * @throws EntityNotFoundException If the travel or users do not exist.
     */
    static void checkIfTravelAndUsersExist(Travel travel, User creator, User recipient, TravelRepository travelRepository, String travelNotFound, UserRepository userRepository, String userNotFound) {
        if (!travelRepository.existsById(travel.getId())) {
            throw new EntityNotFoundException(String.format(travelNotFound, travel.getId()));
        }
        if (!userRepository.existsById(creator.getId())) {
            throw new EntityNotFoundException(String.format(userNotFound, creator.getId()));
        }
        if (!userRepository.existsById(recipient.getId())) {
            throw new EntityNotFoundException(String.format(userNotFound, recipient.getId()));
        }
    }
}
