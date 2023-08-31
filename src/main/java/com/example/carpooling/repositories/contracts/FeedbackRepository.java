package com.example.carpooling.repositories.contracts;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.Feedback;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @Query("select f from Feedback f where" +
            "(:rating is null or f.rating =:rating) " +
            " and(:comment is null or f.comment =:comment)")
    List<Feedback> findByCriteria(
            @Param("rating") Short rating,
            @Param("comment") String comment,
            Sort sort
    );

    List<Feedback> findByRecipientIs(User user);


    @Query("SELECT f FROM Feedback f WHERE " +
            "(:rating IS NULL  OR f.rating =:rating) " +
            "AND (:creator IS NULL  OR  f.creator.userName LIKE %:creator%) " +
            "AND(:recipient IS NULL OR f.recipient.userName LIKE %:recipient%)" +
            "AND f.isDeleted = false")
    Page<Feedback> findAllPaginated(PageRequest pageRequest,
                                    Sort sort,
                                    Short rating,
                                    String creator,
                                    String recipient);

    @Modifying
    @Query("UPDATE Feedback AS f SET f.isDeleted=true WHERE f.id = :id")
    void delete(@Param("id") Long id) throws EntityNotFoundException;


}
