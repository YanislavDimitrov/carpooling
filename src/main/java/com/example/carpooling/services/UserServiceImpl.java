package com.example.carpooling.services;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.User;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.UserService;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUserName(username);
    }

    public void delete(Long id) {
        this.userRepository.deleteUserById(id);
    }

    public Long count() {
        return this.userRepository.count();
    }

    public List<User> getByFirstName(String firstName) {
        return this.userRepository.findAllByFirstNameEquals(firstName);
    }

    public List<User> findByCriteria(String firstName, String lastName, String username, String email, String phoneNumber, Sort sort) {
        return this.userRepository.findByCriteria(firstName, lastName, username, email, phoneNumber, sort);
    }
}
