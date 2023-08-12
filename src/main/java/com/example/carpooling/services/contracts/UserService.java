package com.example.carpooling.services.contracts;

import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.UserCreateDto;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface UserService {
    List<User> getAll();

    User getById(Long id);
    User getByUsername(String username);

    void delete(Long id);

    Long count();

    User create(User user);

    List<User> findAll(Sort sort);

    List<User> findAll(String firstName, String lastName, String username, String email, String phoneNumber, Sort sort);
}
