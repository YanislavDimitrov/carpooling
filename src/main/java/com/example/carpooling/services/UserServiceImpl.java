package com.example.carpooling.services;

import com.example.carpooling.exceptions.*;
import com.example.carpooling.exceptions.duplicate.DuplicateEmailException;
import com.example.carpooling.exceptions.duplicate.DuplicatePhoneNumberException;
import com.example.carpooling.exceptions.duplicate.DuplicateUsernameException;
import com.example.carpooling.models.Feedback;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.Vehicle;
import com.example.carpooling.models.dtos.UserChangePasswordDto;
import com.example.carpooling.models.dtos.UserUpdateDto;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.models.enums.UserStatus;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.repositories.contracts.VehicleRepository;
import com.example.carpooling.services.contracts.UserService;
import com.example.carpooling.services.contracts.ValidationService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.example.carpooling.helpers.ConstantMessages.*;

@Service
public class UserServiceImpl implements UserService {

    public static final String CONFIRMATION_EMAIL_TEMPLATE_PATH = "src/main/resources/templates/WelcomeTemplateEmail.html";
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ValidationService validationService;


    @Autowired
    public UserServiceImpl(UserRepository userRepository, VehicleRepository vehicleRepository, ValidationService validationService) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.validationService = validationService;
    }

    @Override
    public List<User> findAll(Sort sort) {
        return this.userRepository.findAll(sort);
    }

    @Override
    public List<User> findAll(String firstName, String lastName, String username, String email, String phoneNumber, String userRole, String userStatus, Sort sort) {
        return this.userRepository.findByCriteria(firstName, lastName, username, email, phoneNumber, userRole, userStatus, sort);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
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
    public User create(User user) throws MessagingException, IOException {
        checkForDuplicateUser(user);
        this.userRepository.save(user);

        this.validationService.validate(user);

        return user;
    }

    @Override
    public User update(Long id, UserUpdateDto payloadUser, User loggedUser) {
        Optional<User> optionalTargetUser = this.userRepository.findById(id);

        if (optionalTargetUser.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        User targetUser = optionalTargetUser.get();
        if (isAdmin(loggedUser) || areSameUser(loggedUser, targetUser)) {

            targetUser.setFirstName(payloadUser.getFirstName());
            targetUser.setLastName(payloadUser.getLastName());
            targetUser.setEmail(payloadUser.getEmail());
            targetUser.setPhoneNumber(payloadUser.getPhoneNumber());
            targetUser.setUserName(payloadUser.getUserName());

            checkForDuplicateUser(targetUser);
            return this.userRepository.save(targetUser);
        } else {
            throw new AuthorizationException(
                    String.format(UPDATE_USER_AUTHORIZATION_MESSAGE
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

        User userToDelete = optionalUserToDelete.get();
        if (areSameUser(loggedUser, userToDelete)) {
            this.userRepository.delete(id);
            deleteUserFeedbacks(userToDelete);
            deleteUserTravels(userToDelete);
        } else {
            throw new AuthorizationException(
                    String.format(DELETE_USER_AUTHORIZATION_MESSAGE
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

        User userToRestore = optionalUserToRestore.get();

        if (areSameUser(loggedUser, optionalUserToRestore.get())) {
            this.userRepository.restore(id);
            recoverUserFeedbacks(userToRestore);
            recoverUserTravels(userToRestore);
        } else {
            throw new AuthorizationException(
                    String.format(DELETE_USER_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    @Transactional
    @Override
    public void block(Long id, User loggedUser) {
        Optional<User> optionalUserToBlock = this.userRepository.findById(id);

        if (optionalUserToBlock.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        User userToBlock = optionalUserToBlock.get();

        if (isAdmin(loggedUser)) {
            this.userRepository.block(id);
            deleteUserFeedbacks(userToBlock);
            deleteUserTravels(userToBlock);
        } else {
            throw new AuthorizationException(
                    String.format(BLOCK_USER_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }


    @Transactional
    @Override
    public void unblock(Long id, User loggedUser) {
        Optional<User> optionalUserToUnblock = this.userRepository.findById(id);

        if (optionalUserToUnblock.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        User userToUnlock = optionalUserToUnblock.get();

        if (isAdmin(loggedUser)) {
            this.userRepository.restore(id);
            recoverUserFeedbacks(userToUnlock);
            recoverUserTravels(userToUnlock);
        } else {
            throw new AuthorizationException(
                    String.format(UNBLOCK_USER_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    @Override
    @Transactional
    public void upgrade(Long id, User loggedUser) {
        if (!loggedUser.getRole().equals(UserRole.ADMIN)) {
            throw new AuthorizationException(String.format(UPGRADE_USER_AUTHORIZATION_MESSAGE,
                    loggedUser.getUserName(),
                    id));
        }
        Optional<User> optionalTargetUser = this.userRepository.findById(id);

        if (optionalTargetUser.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        this.userRepository.upgrade(id);
    }

    @Override
    @Transactional
    public void downgrade(Long id, User loggedUser) {
        if (!loggedUser.getRole().equals(UserRole.ADMIN)) {
            throw new AuthorizationException(String.format(DOWNGRADE_USER_AUTHORIZATION_MESSAGE,
                    loggedUser.getUserName(),
                    id));
        }
        Optional<User> optionalTargetUser = this.userRepository.findById(id);

        if (optionalTargetUser.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }
        this.userRepository.downgrade(id);
    }

    @Override
    @Transactional
    public void verify(Long id) {
        Optional<User> optionalUserToValidate = this.userRepository.findById(id);

        if (optionalUserToValidate.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        this.userRepository.validate(id);

    }

    @Override
    @Transactional
    public void changePassword(User targetUser, UserChangePasswordDto dto, User loggedUser) {
        if (!isAdmin(loggedUser) && !areSameUser(loggedUser, targetUser)) {
            throw new AuthorizationException(
                    String.format(UPGRADE_USER_PASSWORD_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , targetUser.getId()));
        }
        // Check if Old password is the current one
        if (!targetUser.getPassword().equals(dto.getOldPassword())) {
            throw new WrongPasswordException(WRONG_PASSWORD_MSG);
        }
        // Check if New password matches Confirm New password
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new PasswordMismatchException(PASSWORD_MISMATCH_MSG);
        }
        this.userRepository.changePassword(targetUser.getId(), dto.getNewPassword());
    }

    @Override
    public Page<User> findAllPaginated(int page, int size, String firstName, String lastName, String username, String email, String phoneNumber, String userRole, String userStatus, Sort sort) {
        PageRequest pageRequest = PageRequest.of(page, size);

        UserRole criteriaRole =
                userRole != null && !userRole.trim().isEmpty()
                        ? UserRole.valueOf(userRole.toUpperCase())
                        : null;

        UserStatus criteriaStatus =
                userStatus != null && !userStatus.trim().isEmpty()
                        ? UserStatus.valueOf(userStatus.toUpperCase())
                        : null;

        return userRepository.findAllPaginated(pageRequest, firstName, lastName, username, email, phoneNumber, criteriaRole, criteriaStatus, sort);
    }

    @Override
    public List<User> findTopTenDrivers() {
        return userRepository.findTopTenDrivers();
    }

    @Override
    public List<User> findTopTenPassengers() {
        return userRepository.findTopTenPassengers();
    }

    @Override
    @Transactional
    public void unverify(Long id) {
        Optional<User> optionalUserToInvalidate = this.userRepository.findById(id);

        if (optionalUserToInvalidate.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        this.userRepository.invalidate(id);
    }


    @Override
    public List<Vehicle> getVehiclesByUserId(Long id, User loggedUser) {
        Optional<User> optionalTargetUser = this.userRepository.findById(id);

        if (optionalTargetUser.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        if (isAdmin(loggedUser) || areSameUser(loggedUser, optionalTargetUser.get())) {
            return this.vehicleRepository.findAllByOwnerId(id);
        } else {
            throw new AuthorizationException(
                    String.format(GET_VEHICLES_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    @Override
    public Vehicle addVehicle(Long id, Vehicle payloadVehicle, User loggedUser) {
        Optional<User> optionalTargetUser = this.userRepository.findById(id);

        if (optionalTargetUser.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }
        User targetUser = optionalTargetUser.get();
        if (isAdmin(loggedUser) || areSameUser(loggedUser, targetUser)) {
            payloadVehicle.setOwner(targetUser);
            targetUser.addVehicle(payloadVehicle);
            this.userRepository.save(targetUser);
            return this.vehicleRepository.save(payloadVehicle);
        } else {
            throw new AuthorizationException(
                    String.format(CREATE_VEHICLE_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    public Long count() {
        return this.userRepository.count();
    }

    private void checkForDuplicateUser(User user) {
        User userWithUserName =
                this.userRepository.findByUserName(user.getUserName());
        if (userWithUserName != null) {
            if (!userWithUserName.getId().equals(user.getId())) {
                throw new DuplicateUsernameException("User", user.getUserName());
            }
        }
        User userWithEmail =
                this.userRepository.findByEmail(user.getEmail());
        if (userWithEmail != null) {
            if (!userWithEmail.getId().equals(user.getId())) {
                throw new DuplicateEmailException("User", user.getEmail());
            }
        }
        User userWithPhoneNumber =
                this.userRepository.findByPhoneNumber(user.getPhoneNumber());
        if (userWithPhoneNumber != null) {
            if (!userWithPhoneNumber.getId().equals(user.getId())) {
                throw new DuplicatePhoneNumberException("User", user.getPhoneNumber());
            }
        }
    }

    private boolean isAdmin(User loggedUser) {
        return loggedUser.getRole().equals(UserRole.ADMIN);
    }

    private boolean areSameUser(User loggedUser, User targetUser) {
        return targetUser.getId().equals(loggedUser.getId());
    }

    private void deleteUserFeedbacks(User targetUser) {
        for (Feedback feedback : targetUser.getFeedbacks()) {
            feedback.setDeleted(true);
        }
    }

    private void recoverUserFeedbacks(User targetUser) {
        for (Feedback feedback : targetUser.getFeedbacks()) {
            feedback.setDeleted(false);
        }
    }

    private void deleteUserTravels(User targetUser) {
        for (Travel travel : targetUser.getTravelsAsDriver()) {

            if (travel.getStatus().equals(TravelStatus.ACTIVE)) {
                throw new ActiveTravelException(ACTIVE_TRAVEL_EXCEPTION_MSG);
            }

            if (travel.getStatus().equals(TravelStatus.PLANNED)) {
                travel.setStatus(TravelStatus.CANCELED);
            }

            travel.setDeleted(true);
        }
    }


    private void recoverUserTravels(User targetUser) {
        for (Travel travel : targetUser.getTravelsAsDriver()) {
            travel.setDeleted(false);
        }

    }
}
