package com.example.carpooling.repositories.contracts;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TravelRepository extends JpaRepository<Travel, Long> {

    @Modifying
    @Query("UPDATE Travel AS t SET t.status='DELETED' WHERE t.id = :id")
    void delete(@Param("id") Long id) throws EntityNotFoundException;

    @Modifying
    @Query("UPDATE Travel AS t SET t.status='COMPLETED' WHERE t.id = :id")
    void completeTravel(@Param("id") Long id) throws EntityNotFoundException;

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

}
