package com.example.carpooling.repositories.contracts;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByFirstNameEquals(String firstName);

    @Modifying
    @Query("UPDATE User AS u SET u.status='DELETED' WHERE u.id = :id")
    void deleteUserById(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE " +
            "(:firstName IS NULL OR u.firstName LIKE %:firstName%) " +
            "AND (:lastName IS NULL OR u.lastName LIKE %:lastName%) " +
            "AND (:username IS NULL OR u.userName LIKE %:username%) " +
            "AND (:email IS NULL OR u.email LIKE :email) " +
            "AND (:phoneNumber IS NULL OR u.phoneNumber = :phoneNumber) ")
    List<User> findByCriteria(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("username") String username,
            @Param("email") String email,
            @Param("phoneNumber") String phoneNumber,
            Sort sort
    );
    User findByUserName(String username) throws EntityNotFoundException;
}
