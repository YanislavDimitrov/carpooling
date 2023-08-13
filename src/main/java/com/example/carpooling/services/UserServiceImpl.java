package com.example.carpooling.services;

import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.DuplicateEntityException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.UserUpdateDto;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.carpooling.helpers.CustomMessages.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> findAll(Sort sort) {
        return this.userRepository.findAll();
    }

    @Override
    public List<User> findAll(String firstName, String lastName, String username, String email, String phoneNumber, Sort sort) {
        return this.userRepository.findByCriteria(firstName, lastName, username, email, phoneNumber, sort);
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


    @Override
    public User create(User user) {
        checkForDuplicateUser(user);
        return this.userRepository.save(user);
    }

    @Override
    public User update(Long id, UserUpdateDto payloadUser, User loggedUser) {
        Optional<User> optionalTargetUser = this.userRepository.findById(id);

        if (optionalTargetUser.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        User targetUser = optionalTargetUser.get();
        if (isAdmin(loggedUser) || isSameUser(loggedUser, targetUser)) {

            targetUser.setFirstName(payloadUser.getFirstName());
            targetUser.setLastName(payloadUser.getLastName());
            targetUser.setEmail(payloadUser.getEmail());
            targetUser.setPhoneNumber(payloadUser.getPhoneNumber());
            targetUser.setUserName(payloadUser.getUserName());

            checkForDuplicateUser(targetUser);
            return this.userRepository.save(targetUser);
        } else {
            throw new AuthorizationException(
                    String.format(UPDATE_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    @Override
    @Transactional
    public void delete(Long id, User loggedUser) {

        Optional<User> optionalUserToDelete = this.userRepository.findById(id);

        if (optionalUserToDelete.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        if (isAdmin(loggedUser) || isSameUser(loggedUser, optionalUserToDelete.get())) {
            this.userRepository.delete(id);
        } else {
            throw new AuthorizationException(
                    String.format(DELETE_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    @Override
    @Transactional
    public void restore(Long id, User loggedUser) {
        Optional<User> optionalUserToRestore = this.userRepository.findById(id);

        if (optionalUserToRestore.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        if (isAdmin(loggedUser) || isSameUser(loggedUser, optionalUserToRestore.get())) {
            this.userRepository.restore(id);
        } else {
            throw new AuthorizationException(
                    String.format(DELETE_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }


    public Long count() {
        return this.userRepository.count();
    }

    private void checkForDuplicateUser(User user) {
        List<User> userWithUserName =
                this.findAll(null, null, user.getUserName(), null, null, null);
        if (!userWithUserName.isEmpty()) {
            if (!userWithUserName.get(0).getId().equals(user.getId())) {
                throw new DuplicateEntityException("User", "username", user.getUserName());
            }
        }
        List<User> userWithEmail =
                this.findAll(null, null, null, user.getEmail(), null, null);
        if (!userWithEmail.isEmpty()) {
            if (!userWithEmail.get(0).getId().equals(user.getId())) {
                throw new DuplicateEntityException("User", "email", user.getEmail());
            }
        }
        List<User> userWithPhoneNumber =
                this.findAll(null, null, null, null, user.getPhoneNumber(), null);
        if (!userWithPhoneNumber.isEmpty()) {
            if (!userWithPhoneNumber.get(0).getId().equals(user.getId())) {
                throw new DuplicateEntityException("User", "phone number", user.getPhoneNumber());
            }
        }
    }

    private boolean isAdmin(User loggedUser) {
        return loggedUser.getRole().equals(UserRole.ADMIN);
    }

    private static boolean isSameUser(User loggedUser, User userToDelete) {
        return userToDelete.getUserName().equals(loggedUser.getUserName());
    }
}
