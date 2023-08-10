package com.example.carpooling.repositories.contracts;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TravelRepository extends JpaRepository<Travel, Long> {

    List<Travel> findAllByDriverIs(User user) throws EntityNotFoundException;

    List<Travel> findAllByStatus(TravelStatus status) throws EntityNotFoundException;

    List<Travel> findAllByFreeSpots(int freeSpots) throws EntityNotFoundException;

    @Modifying
    @Query("UPDATE Travel AS t SET t.status='INACTIVE' WHERE t.id = :id")
    void delete(@Param("id") Long id) throws EntityNotFoundException;

    @Modifying
    @Query("UPDATE Travel AS t SET t.status='COMPLETED' WHERE t.id = :id")
    void completeTravel(@Param("id") Long id) throws  EntityNotFoundException;
}
