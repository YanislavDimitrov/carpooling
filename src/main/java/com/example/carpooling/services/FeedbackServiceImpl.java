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
import java.util.Optional;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    public static final String FEEDBACK_NOT_FOUND = "Feedback with ID %d was not found!";
    public static final String TRAVEL_NOT_FOUND = "Travel with ID %d was not found!";
    public static final String USER_NOT_FOUND = "User with ID %d was not found";
    public static final String TRAVEL_NOT_COMPLETED = "Travel with ID %d has not been completed so you cannot leave feedback now.";
    public static final String NOT_AUTHORIZED = "You are not authorized to update feedback because only creators of the feedback can update it!";
    public static final String INVALID_FEEDBACK = "You cannot give feedback for this person if he was not part of this travel!";
    public static final String FEEDBACK_REPETITION = "You cannot give feedback again for the same person for this travel,if you want to edit your feedback , please go to the edit button!";
    public static final String CANNOT_GIVE_YOURSELF_A_FEEDBACK = "You cannot give yourself a feedback!";
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

    @Override
    public List<Feedback> get() {
        return feedbackRepository.findAll();
    }

    @Override
    public Feedback getById(Long id) {
        return feedbackRepository
                .findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException(String.format(FEEDBACK_NOT_FOUND, id))
                );
    }

    @Override
    public List<Feedback> getByRecipientIs(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND, user.getId()));
        }
        return feedbackRepository.findNonDeletedFeedbacksForRecipient(user);
    }

    @Override
    public List<Feedback> findByCriteria(Short rating, String comment, Sort sort) {
        return feedbackRepository.findByCriteria(rating, comment, sort);
    }

    @Override
    public Page<Feedback> findAllPaginated(int page, int size, Sort sort, Short rating, User creator, User recipient, Travel travel) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return feedbackRepository.findAllPaginated(pageRequest, sort, rating, creator, recipient, travel);
    }

    @Override
    public List<Feedback> findByTravelId(Long id) {
        if (!travelRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, id));
        }
        return feedbackRepository.findByTravelId(id);
    }

    @Override
    public boolean existsByTravelAndRecipientAndCreator(Travel travel, User recipient, User creator) {
        return feedbackRepository.existsByTravelAndRecipientAndCreator(travel, recipient, creator);
    }

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

    @Override
    public List<Feedback> findAll(Sort sort) {
        return feedbackRepository.findAll();
    }

    @Override
    public Long count() {
        return feedbackRepository.count();
    }

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

    private boolean haveTravelledTogether(Long travelId, User driver, User recipient) {
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
