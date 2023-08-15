package com.example.carpooling.services.contracts;

import com.example.carpooling.models.Feedback;
import com.example.carpooling.models.Travel;
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

    void create(Feedback feedback);

    void update(Long id);

    void delete(Long id);
}
