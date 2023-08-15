package com.example.carpooling.services.contracts;

import com.example.carpooling.models.Feedback;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelStatus;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

public interface FeedbackService {
    List<Feedback> get();

    Feedback getById(Long id);

    List<Feedback> findByCriteria(Short rating,
                                String comment,
                                Sort sort);
    List<Feedback> findAll(Sort sort);

    Long count ();

    Feedback create(Travel travel , User creator , User recipient , Feedback feedback);

    Feedback update(Feedback originalFeedback , Feedback updateFeedback , User editor);

    void delete(Long id,User editor);
}
