package com.example.carpooling.services;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.Feedback;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.repositories.contracts.FeedbackRepository;
import com.example.carpooling.services.contracts.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    public static final String FEEDBACK_NOT_FOUND = "Feedback with ID %f was not found!";
    private final FeedbackRepository feedbackRepository;

    @Autowired
    public FeedbackServiceImpl(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
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
    public List<Feedback> findByCriteria(String driver, TravelStatus status, Short freeSpots, LocalDateTime departureTime, Sort sort) {
        return null;
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
    public void create(Feedback feedback) {
        feedbackRepository.save(feedback);
    }

    @Override
    public void update(Long id) {

    }

    @Override
    public void delete(Long id) {

    }
}
