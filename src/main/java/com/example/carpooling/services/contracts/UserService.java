package com.example.carpooling.services.contracts;

import com.example.carpooling.models.User;

import java.util.List;

public interface UserService {
    List<User> getAll();

    User getById(Long id);

    void delete(Long id);

    Long count();
}
