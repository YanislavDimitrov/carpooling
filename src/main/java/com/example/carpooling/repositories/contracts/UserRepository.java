package com.example.carpooling.repositories.contracts;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.User;
import com.example.carpooling.models.Vehicle;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE " +
            "(:firstName IS NULL OR u.firstName LIKE %:firstName%) " +
            "AND (:lastName IS NULL OR u.lastName LIKE %:lastName%) " +
            "AND (:username IS NULL OR u.userName LIKE %:username%) " +
            "AND (:email IS NULL OR u.email LIKE :email) " +
            "AND (:phoneNumber IS NULL OR u.phoneNumber = :phoneNumber) ")
    List<User> findByCriteria(
            String firstName,
            String lastName,
            String username,
            String email,
            String phoneNumber,
            Sort sort
    );

    User findByUserName(String username);

    @Modifying
    @Query("UPDATE User AS u SET u.status='DELETED' WHERE u.id = :id")
    void delete(Long id);

    @Modifying
    @Query("UPDATE User AS u SET u.status='ACTIVE' WHERE u.id = :id")
    void restore(Long id);
    @Modifying
    @Query("UPDATE User AS u SET u.status='BLOCKED' WHERE u.id = :id")
    void block(Long id);
}
