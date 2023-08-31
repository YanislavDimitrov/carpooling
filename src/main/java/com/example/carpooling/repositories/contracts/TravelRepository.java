package com.example.carpooling.repositories.contracts;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.enums.TravelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TravelRepository extends JpaRepository<Travel, Long> {
    @Query("select t from Travel t where  t.isDeleted=false")
    List<Travel> getAll();

    List<Travel> getAllByStatusIs(TravelStatus status);

    List<Travel> findTop5ByOrderByDepartureTimeDesc();

    List<Travel> findByDriver_Id(Long id);

    @Query("select t from Travel t where" +
            "(:driver is null or t.driver.userName like %:driver%) " +
            " and(:status is null or t.status =:status)" +
            " and (:free_spots is null or t.freeSpots  =:freeSpots)" +
            " and(:departureTime is null or t.departureTime =:departureTime)")
    List<Travel> findByCriteria(
            @Param("driver") String driver,
            @Param("status") TravelStatus status,
            @Param("freeSpots") Short freeSpots,
            @Param("departureTime") LocalDateTime departureTime,
            Sort sort
    );

    @Query("SELECT t FROM Travel t " +
            "WHERE (:departurePoint IS NULL OR :departurePoint = '' OR t.departurePoint LIKE %:departurePoint%) " +
            "AND (:arrivalPoint IS NULL OR :arrivalPoint = '' OR t.arrivalPoint LIKE %:arrivalPoint%) " +
            "AND (:departureTime IS NULL OR t.departureTime = :departureTime) " +
            "AND (:freeSpots IS NULL OR t.freeSpots >= :freeSpots)")
    List<Travel> findByCustomSearchFilter(
            String departurePoint,
            String arrivalPoint,
            LocalDateTime departureTime,
            Short freeSpots
    );

    List<Travel> findByStatusAndDepartureTimeBefore(TravelStatus status, LocalDateTime departureTime);

    @Query("SELECT t FROM Travel t WHERE " +
            "(:freeSpots IS NULL  OR t.freeSpots >=:freeSpots) " +
            "AND (:departedBefore IS NULL  OR FUNCTION('DATE', t.departureTime) >= :departedBefore) " +
            "AND(:departedAfter IS NULL OR FUNCTION('DATE', t.departureTime) < :departedAfter)" +
            "AND (:departurePoint IS NULL OR :departurePoint = '' OR t.departurePoint LIKE %:departurePoint%) " +
            "AND(:arrivalPoint IS NULL OR :arrivalPoint = '' OR t.arrivalPoint LIKE %:arrivalPoint%)" +
            "AND (:price IS NULL OR :price = '' OR t.price LIKE :price) ")
    Page<Travel> findAllPaginated(PageRequest pageRequest,
                                  Sort sort,
                                  Short freeSpots,
                                  LocalDate departedBefore,
                                  LocalDate departedAfter,
                                  String departurePoint,
                                  String arrivalPoint,
                                  String price);

    @Modifying
    @Query("UPDATE Travel AS t SET t.isDeleted=true WHERE t.id = :id")
    void delete(@Param("id") Long id) throws EntityNotFoundException;

    @Modifying
    @Query("UPDATE Travel AS t SET t.status='COMPLETED' WHERE t.id = :id")
    void completeTravel(@Param("id") Long id) throws EntityNotFoundException;

    Long countAllByStatusIs(TravelStatus status);


}

