package com.example.carpooling.services.contracts;

import com.example.carpooling.models.User;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface UserService {
    List<User> findAll(Sort sort);

    List<User> findAll(String firstName, String lastName, String username, String email, String phoneNumber, Sort sort);

    User getById(Long id);

    User getByUsername(String username);

    User create(User user);

    void delete(Long id, User loggedUser);

    Long count();

    void restore(Long id, User loggedUser);
}
