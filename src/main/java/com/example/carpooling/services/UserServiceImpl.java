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

/**
 * The {@code UserServiceImpl} class is responsible for managing user-related operations in the carpooling application.
 * It provides methods for user creation, update, deletion, and various other user management functionalities.
 * <p>
 * This class is annotated with {@code @Service} to indicate that it is a Spring service component, making it eligible
 * for automatic dependency injection.
 *
 * @author Yanislav Dimitrov & Ivan Boev
 * @version 1.0
 * @since 06.09.23
 */
@Service
public class UserServiceImpl implements UserService {

    public static final String CONFIRMATION_EMAIL_TEMPLATE_PATH = "src/main/resources/templates/WelcomeTemplateEmail.html";
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ValidationService validationService;

    /**
     * Constructs an instance of the UserServiceImpl class with the necessary dependencies.
     *
     * @param userRepository    The repository for storing and managing user data.
     * @param vehicleRepository The repository for storing and managing vehicle data.
     * @param validationService The service responsible for user validation.
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository, VehicleRepository vehicleRepository, ValidationService validationService) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.validationService = validationService;
    }

    /**
     * Retrieves a list of all users sorted according to the specified sorting criteria.
     *
     * @param sort The sorting criteria to apply to the user list.
     * @return A list of users sorted as specified.
     */
    @Override
    public List<User> findAll(Sort sort) {
        return this.userRepository.findAll(sort);
    }

    /**
     * Retrieves a list of users based on the provided search criteria and sorting order.
     *
     * @param firstName   The first name of the user to search for.
     * @param lastName    The last name of the user to search for.
     * @param username    The username of the user to search for.
     * @param email       The email address of the user to search for.
     * @param phoneNumber The phone number of the user to search for.
     * @param userRole    The role of the user to search for (e.g., "USER" or "ADMIN").
     * @param userStatus  The status of the user to search for (e.g., "ACTIVE" or "BLOCKED").
     * @param sort        The sorting criteria to apply to the user list.
     * @return A list of users matching the specified criteria, sorted as specified.
     */
    @Override
    public List<User> findAll(String firstName, String lastName, String username, String email, String phoneNumber, String userRole, String userStatus, Sort sort) {
        return this.userRepository.findByCriteria(firstName, lastName, username, email, phoneNumber, userRole, userStatus, sort);
    }

    /**
     * Retrieves a list of all users without any specific sorting order.
     *
     * @return A list of all users in no particular order.
     */
    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id The unique identifier of the user to retrieve.
     * @return The user entity associated with the provided ID.
     * @throws EntityNotFoundException If no user with the given ID is found in the repository.
     */
    public User getById(Long id) {
        Optional<User> optionalUser = this.userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new EntityNotFoundException("User");
        }
        return optionalUser.get();
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user to retrieve.
     * @return The user entity associated with the provided username.
     * @throws EntityNotFoundException If no user with the given username is found in the repository.
     */
    @Override
    public User getByUsername(String username) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new EntityNotFoundException("User", "username", username);
        }
        return user;
    }

    /**
     * Creates a new user entity and saves it to the repository.
     *
     * @param user The user entity to create and save.
     * @return The created user entity.
     * @throws MessagingException            If there is an issue with sending a validation email (if applicable).
     * @throws IOException                   If there is an issue with reading email templates (if applicable).
     * @throws DuplicateUsernameException    If a user with the same username already exists.
     * @throws DuplicateEmailException       If a user with the same email address already exists.
     * @throws DuplicatePhoneNumberException If a user with the same phone number already exists.
     */
    @Override
    @Transactional
    public User create(User user) throws MessagingException, IOException {
        checkForDuplicateUser(user);
        this.userRepository.save(user);

        this.validationService.validate(user);

        return user;
    }

    /**
     * Updates an existing user's information based on the provided data.
     *
     * @param id          The unique identifier of the user to update.
     * @param payloadUser The DTO containing the updated user information.
     * @param loggedUser  The user performing the update.
     * @return The updated user entity.
     * @throws EntityNotFoundException       If no user with the given ID is found in the repository.
     * @throws AuthorizationException        If the logged user is not authorized to perform the update.
     * @throws DuplicateUsernameException    If the update results in a duplicate username.
     * @throws DuplicateEmailException       If the update results in a duplicate email address.
     * @throws DuplicatePhoneNumberException If the update results in a duplicate phone number.
     */
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

    /**
     * Deletes a user from the system based on the provided user ID.
     *
     * @param id         The unique identifier of the user to delete.
     * @param loggedUser The user performing the deletion.
     * @throws EntityNotFoundException If no user with the given ID is found in the repository.
     * @throws AuthorizationException  If the logged user is not authorized to perform the deletion.
     */
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

    /**
     * Restores a previously deleted user in the system based on the provided user ID.
     *
     * @param id         The unique identifier of the user to restore.
     * @param loggedUser The user performing the restoration.
     * @throws EntityNotFoundException If no user with the given ID is found in the repository.
     * @throws AuthorizationException  If the logged user is not authorized to perform the restoration.
     */
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

    /**
     * Blocks a user in the system based on the provided user ID.
     *
     * @param id         The unique identifier of the user to block.
     * @param loggedUser The user performing the blocking.
     * @throws EntityNotFoundException If no user with the given ID is found in the repository.
     * @throws AuthorizationException  If the logged user is not authorized to perform the blocking.
     * @throws ActiveTravelException   If the user has active travel records, blocking is not allowed.
     */
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

    /**
     * Unblocks a previously blocked user in the system based on the provided user ID.
     *
     * @param id         The unique identifier of the user to unblock.
     * @param loggedUser The user performing the unblocking.
     * @throws EntityNotFoundException If no user with the given ID is found in the repository.
     * @throws AuthorizationException  If the logged user is not authorized to perform the unblocking.
     */
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

    /**
     * Upgrades a user's role to an admin role in the system based on the provided user ID.
     *
     * @param id         The unique identifier of the user to upgrade.
     * @param loggedUser The user performing the upgrade.
     * @throws AuthorizationException  If the logged user is not authorized to perform the upgrade.
     * @throws EntityNotFoundException If no user with the given ID is found in the repository.
     */
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

    /**
     * Downgrades a user's role from admin to a regular user in the system based on the provided user ID.
     *
     * @param id         The unique identifier of the user to downgrade.
     * @param loggedUser The user performing the downgrade.
     * @throws AuthorizationException  If the logged user is not authorized to perform the downgrade.
     * @throws EntityNotFoundException If no user with the given ID is found in the repository.
     */
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

    /**
     * Verifies a user in the system based on the provided user ID.
     *
     * @param id The unique identifier of the user to verify.
     * @throws EntityNotFoundException If no user with the given ID is found in the repository.
     */
    @Override
    @Transactional
    public void verify(Long id) {
        Optional<User> optionalUserToValidate = this.userRepository.findById(id);

        if (optionalUserToValidate.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        this.userRepository.validate(id);

    }

    /**
     * Changes the password of a user in the system.
     *
     * @param targetUser The user whose password is to be changed.
     * @param dto        The data transfer object containing the old and new passwords.
     * @param loggedUser The user performing the password change.
     * @throws AuthorizationException    If the logged user is not authorized to perform the password change.
     * @throws WrongPasswordException    If the old password provided does not match the current password of the user.
     * @throws PasswordMismatchException If the new password and confirm new password do not match.
     */
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

    /**
     * Retrieves a paginated list of users based on filtering criteria and sorting options.
     *
     * @param page        The page number (0-based) of the results to retrieve.
     * @param size        The number of results to retrieve per page.
     * @param firstName   Filter by user's first name (optional).
     * @param lastName    Filter by user's last name (optional).
     * @param username    Filter by user's username (optional).
     * @param email       Filter by user's email (optional).
     * @param phoneNumber Filter by user's phone number (optional).
     * @param userRole    Filter by user's role (optional).
     * @param userStatus  Filter by user's status (optional).
     * @param sort        The sorting criteria for the results.
     * @return A paginated list of users that match the given criteria.
     */
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

    /**
     * Retrieves a list of the top ten users with the role of "Driver" based on certain criteria.
     *
     * @return A list of the top ten drivers.
     */
    @Override
    public List<User> findTopTenDrivers() {
        return userRepository.findTopTenDrivers();
    }

    /**
     * Retrieves a list of the top ten users with the role of "Passenger" based on certain criteria.
     *
     * @return A list of the top ten passengers.
     */
    @Override
    public List<User> findTopTenPassengers() {
        return userRepository.findTopTenPassengers();
    }

    /**
     * Unverifies a previously verified user in the system based on the provided user ID.
     *
     * @param id The unique identifier of the user to unverify.
     * @throws EntityNotFoundException If no user with the given ID is found in the repository.
     */
    @Override
    @Transactional
    public void unverify(Long id) {
        Optional<User> optionalUserToInvalidate = this.userRepository.findById(id);

        if (optionalUserToInvalidate.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        this.userRepository.invalidate(id);
    }

    /**
     * Retrieves a list of vehicles owned by a user based on the provided user ID.
     *
     * @param id         The unique identifier of the user for whom vehicles are to be retrieved.
     * @param loggedUser The user performing the operation.
     * @return A list of vehicles owned by the specified user.
     * @throws EntityNotFoundException If no user with the given ID is found in the repository.
     * @throws AuthorizationException  If the logged user is not authorized to retrieve the vehicles.
     */
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

    /**
     * Adds a new vehicle for a user based on the provided user ID and vehicle information.
     *
     * @param id             The unique identifier of the user to whom the vehicle is to be added.
     * @param payloadVehicle The vehicle information to be added.
     * @param loggedUser     The user performing the operation.
     * @return The newly added vehicle.
     * @throws EntityNotFoundException If no user with the given ID is found in the repository.
     * @throws AuthorizationException  If the logged user is not authorized to add a vehicle for the specified user.
     */
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

    /**
     * Retrieves the total number of users in the system.
     *
     * @return The total number of users.
     */
    public Long count() {
        return this.userRepository.count();
    }

    /**
     * Checks for duplicate user information (username, email, and phone number) in the repository
     * to ensure there are no conflicts when creating or updating a user.
     *
     * @param user The user for whom duplicate information is checked.
     * @throws DuplicateUsernameException    If another user with the same username already exists.
     * @throws DuplicateEmailException       If another user with the same email address already exists.
     * @throws DuplicatePhoneNumberException If another user with the same phone number already exists.
     */
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

    /**
     * Checks if the logged-in user has an admin role.
     *
     * @param loggedUser The user whose role is checked.
     * @return True if the user is an admin; otherwise, false.
     */
    private boolean isAdmin(User loggedUser) {
        return loggedUser.getRole().equals(UserRole.ADMIN);
    }

    /**
     * Checks if two users are the same by comparing their unique identifiers.
     *
     * @param loggedUser The first user.
     * @param targetUser The second user.
     * @return True if the users are the same; otherwise, false.
     */
    private boolean areSameUser(User loggedUser, User targetUser) {
        return targetUser.getId().equals(loggedUser.getId());
    }

    /**
     * Marks all feedback associated with a user as deleted.
     *
     * @param targetUser The user for whom feedback is marked as deleted.
     */
    private void deleteUserFeedbacks(User targetUser) {
        for (Feedback feedback : targetUser.getFeedbacks()) {
            feedback.setDeleted(true);
        }
    }

    /**
     * Recovers (unmarks as deleted) all feedback associated with a user.
     *
     * @param targetUser The user for whom feedback is recovered.
     */
    private void recoverUserFeedbacks(User targetUser) {
        for (Feedback feedback : targetUser.getFeedbacks()) {
            feedback.setDeleted(false);
        }
    }

    /**
     * Marks all travels associated with a user as deleted and handles exceptions for active travels.
     *
     * @param targetUser The user for whom travels are marked as deleted.
     * @throws ActiveTravelException If there are active travels associated with the user.
     */
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

    /**
     * Recovers (unmarks as deleted) all travels associated with a user in which the user is the driver.
     *
     * @param targetUser The user for whom travels are recovered.
     */
    private void recoverUserTravels(User targetUser) {
        for (Travel travel : targetUser.getTravelsAsDriver()) {
            travel.setDeleted(false);
        }

    }
}
