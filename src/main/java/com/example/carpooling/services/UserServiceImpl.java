package com.example.carpooling.services;

import com.example.carpooling.models.User;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.UserService;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAll() {
        return this.userRepository.findAll();
    }

    public User getById(Long id) {
        Optional<User> optionalUser = this.userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new EntityExistsException("User");
        }
        return optionalUser.get();
    }

    public void delete(Long id) {
        this.userRepository.deleteById(id);
    }

    public Long count() {
        return this.userRepository.count();
    }
}
