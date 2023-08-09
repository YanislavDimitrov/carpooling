package com.example.carpooling.repositories.contracts;

import com.example.carpooling.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByFirstNameEquals(String firstName);

    @Query("UPDATE User AS u SET u.status='DELETED' WHERE u.id = :id")
    void deleteUserById(@Param("id") Long id);
}
