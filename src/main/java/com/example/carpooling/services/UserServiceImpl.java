package com.example.carpooling.services;

import com.example.carpooling.exceptions.DuplicateEntityException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.UserCreateDto;
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
            throw new EntityNotFoundException("User");
        }
        return optionalUser.get();
    }

    @Override
    public User getByUsername(String username) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new EntityNotFoundException("User", "username", username);
        }
        return user;
    }

    public void delete(Long id) {
        this.userRepository.deleteUserById(id);
    }

    public Long count() {
        return this.userRepository.count();
    }

    @Override
    public User create(User user) {
        checkForDuplicateUser(user);
        return this.userRepository.save(user);
    }


    @Override
    public List<User> findAll(Sort sort) {
        return this.userRepository.findAll();
    }

    @Override
    public List<User> findAll(String firstName, String lastName, String username, String email, String phoneNumber, Sort sort) {
        return this.userRepository.findByCriteria(firstName, lastName, username, email, phoneNumber, sort);
    }

    private void checkForDuplicateUser(User user) {
        List<User> userWithUserName =
                this.findAll(null, null, user.getUserName(), null, null, null);
        if (!userWithUserName.isEmpty()) {
            throw new DuplicateEntityException("User", "username", user.getUserName());
        }
        List<User> userWithEmail =
                this.findAll(null, null, null, user.getEmail(), null, null);
        if (!userWithEmail.isEmpty()) {
            throw new DuplicateEntityException("User", "email", user.getEmail());
        }
        List<User> userWithPhoneNumber =
                this.findAll(null, null, null, null, user.getPhoneNumber(), null);
        if (!userWithPhoneNumber.isEmpty()) {
            throw new DuplicateEntityException("User", "phone number", user.getPhoneNumber());
        }
    }
}
