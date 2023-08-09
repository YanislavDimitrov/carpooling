package com.example.carpooling.services.contracts;

import com.example.carpooling.models.User;

import java.util.List;

public interface UserService {
    List<User> getAll();

    User getById(Long id);

    public void delete(Long id);

    public Long count();
}
