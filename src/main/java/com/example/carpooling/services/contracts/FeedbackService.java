package com.example.carpooling.services.contracts;

import com.example.carpooling.models.Feedback;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface FeedbackService {
    List<Feedback> get();

    Feedback getById(Long id);

    List<Feedback> getByRecipientIs(User user);

    List<Feedback> findByCriteria(Short rating,
                                  String comment,
                                  Sort sort);

    Page<Feedback> findAllPaginated(int page,
                                    int size,
                                    Sort sort,
                                    Short rating,
                                    User creator,
                                    User recipient,
                                    Travel travel);
    List<Feedback> findByTravelId(Long id);

    List<Feedback> findAll(Sort sort);
    boolean existsByTravelAndRecipientAndCreator(Travel travel , User recipient , User creator);
    boolean existsByTravelAndCreator(Travel travel, User creator);
    Feedback findByTravelIsAndCreatorAndRecipient(Travel travel ,User creator , User recipient);


    Feedback create(Travel travel, User creator, User recipient, Feedback feedback);

    Feedback update(Feedback originalFeedback, Feedback updateFeedback, User editor);

    void delete(Long id, User editor);
    Long count();
}
