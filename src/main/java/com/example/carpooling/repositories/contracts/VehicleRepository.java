package com.example.carpooling.repositories.contracts;

import com.example.carpooling.models.User;
import com.example.carpooling.models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findAllByOwnerId(Long id);
    @Modifying
    @Query("UPDATE Vehicle AS v SET v.isDeleted=true WHERE v.id = :id")
    void delete(Long id);
}
