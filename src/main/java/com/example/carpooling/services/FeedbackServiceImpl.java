package com.example.carpooling.services;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.exceptions.TravelNotCompletedException;
import com.example.carpooling.models.Feedback;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.repositories.contracts.FeedbackRepository;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    public static final String FEEDBACK_NOT_FOUND = "Feedback with ID %f was not found!";
    public static final String TRAVEL_NOT_FOUND = "Travel with ID %d was not found!";
    public static final String USER_NOT_FOUND = "User with ID %d was not found";
    public static final String TRAVEL_NOT_COMPLETED = "Travel with ID %d has not been completed so you cannot leave feedback now.";
    private final FeedbackRepository feedbackRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;

    @Autowired
    public FeedbackServiceImpl(FeedbackRepository feedbackRepository, TravelRepository travelRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.travelRepository = travelRepository;
        this.userRepository = userRepository;
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
    public List<Feedback> findByCriteria(Short rating, String comment, Sort sort) {
        return feedbackRepository.findByCriteria(rating, comment, sort);
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
        feedback.setCreator(creator);
        feedback.setRecipient(recipient);
        feedback.setTravel(travel);
        feedbackRepository.save(feedback);
        return feedback;
    }

    @Override
    public void update(Long id) {

    }

    @Override
    public void delete(Long id) {

    }
}
