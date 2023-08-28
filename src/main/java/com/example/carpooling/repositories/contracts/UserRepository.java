package com.example.carpooling.repositories.contracts;

import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.UserPreviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE " +
            "(:firstName IS NULL OR :firstName = '' OR u.firstName LIKE %:firstName%) " +
            "AND (:lastName IS NULL OR :lastName = '' OR u.lastName LIKE %:lastName%) " +
            "AND (:username IS NULL OR :username = '' OR u.userName LIKE %:username%) " +
            "AND (:email IS NULL OR :email = '' OR u.email LIKE :email) " +
            "AND (:phoneNumber IS NULL OR :phoneNumber = '' OR u.phoneNumber = :phoneNumber) ")
    List<User> findByCriteria(
            String firstName,
            String lastName,
            String username,
            String email,
            String phoneNumber,
            Sort sort
    );

    User findByUserName(String username);

    User findByEmail(String email);

    User findByPhoneNumber(String phoneNumber);

    @Modifying
    @Query("UPDATE User AS u SET u.status = 'DELETED' WHERE u.id = :id")
    void delete(Long id);

    @Modifying
    @Query("UPDATE User AS u SET u.status = 'ACTIVE' WHERE u.id = :id")
    void restore(Long id);

    @Modifying
    @Query("UPDATE User AS u SET u.status = 'BLOCKED' WHERE u.id = :id")
    void block(Long id);

    @Modifying
    @Query("UPDATE User AS u SET u.role = 'ADMIN' WHERE  u.id = :id")
    void upgrade(Long id);

    @Modifying
    @Query("UPDATE User AS u SET u.role = 'USER' WHERE  u.id = :id")
    void downgrade(Long id);

    @Modifying
    @Query("UPDATE User AS u SET u.isValidated = true WHERE  u.id = :id")
    void validate(Long id);

    @Modifying
    @Query("UPDATE User AS u SET u.isValidated = false WHERE  u.id = :id")
    void invalidate(Long id);

    @Modifying
    @Query("UPDATE User as u SET u.password = :newPassword WHERE u.id = :id")
    void changePassword(Long id, String newPassword);

    @Query("SELECT u FROM User u WHERE " +
            "(:firstName IS NULL OR :firstName = '' OR u.firstName LIKE %:firstName%) " +
            "AND (:lastName IS NULL OR :lastName = '' OR u.lastName LIKE %:lastName%) " +
            "AND (:username IS NULL OR :username = '' OR u.userName LIKE %:username%) " +
            "AND (:email IS NULL OR :email = '' OR u.email LIKE :email) " +
            "AND (:phoneNumber IS NULL OR :phoneNumber = '' OR u.phoneNumber = :phoneNumber) ")
    Page<User> findAll(
            PageRequest pageRequest,
            String firstName,
            String lastName,
            String username,
            String email,
            String phoneNumber,
            Sort sort);
}
