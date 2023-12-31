package com.example.carpooling.repositories.contracts;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.Feedback;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT f FROM Feedback f WHERE f.recipient = :recipient AND f.isDeleted = false")
    List<Feedback> findNonDeletedFeedbacksForRecipient(@Param("recipient") User recipient);

    List<Feedback> findByTravelId(Long id);


    @Query("SELECT f FROM Feedback f WHERE " +
            "(:rating IS NULL  OR f.rating=0 or f.rating = :rating) " +
            "AND(:creator IS NULL  or :creator = ''  OR  f.creator.userName  like %:creator%) " +
            "AND(:recipient IS NULL OR :recipient = '' or  f.recipient.userName  like %:recipient%)" +
            "AND(:travel IS NULL OR f.travel =:travel)" +
            "AND f.isDeleted = false")
    Page<Feedback> findAllPaginated(PageRequest pageRequest,
                                    Sort sort,
                                    Short rating,
                                    String creator,
                                    String recipient,
                                    Travel travel);

    @Modifying
    @Query("UPDATE Feedback AS f SET f.isDeleted=true WHERE f.id = :id")
    void delete(@Param("id") Long id) throws EntityNotFoundException;


    boolean existsByTravelAndCreator(Travel travel, User creator);

    boolean existsByTravelAndRecipientAndCreator(Travel travel, User recipient, User creator);

    Feedback findByTravelIsAndCreatorAndRecipient(Travel travel, User creator, User recipient);


}
