package com.example.carpooling.controllers.rest;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.DuplicateEntityException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.models.User;
import com.example.carpooling.models.Vehicle;
import com.example.carpooling.models.dtos.*;
import com.example.carpooling.services.contracts.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/users")
public class UserRestController {
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public UserRestController(UserService userService, ModelMapper modelMapper, AuthenticationHelper authenticationHelper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.authenticationHelper = authenticationHelper;
    }

    /**
     * Method returning a Collection of Users that suit a set of filters and search options.
     * <p>
     * Filter and search options are not required and if no such are specified the method will work as a regular "getAll" method
     *
     * @param firstName   element that will filter the users with the provided firstName. (null if not specified)
     * @param lastName    element that will filter the users with the provided lastName. (null if not specified)
     * @param username    element that will filter the users with the provided username. (null if not specified)
     * @param email       element that will filter the users with the provided email. (null if not specified)
     * @param phoneNumber element that will filter the users with the provided phoneNumber. (null if not specified)
     * @param sortBy      specifies the "sort by" parameter
     * @param sortOrder   specifies the sort order (Descending or Ascending)
     * @return List of users that suit all provided set of filtering parameters
     */
    @GetMapping()
    public List<UserViewDto> getAllUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder
    ) {
        Sort sort;
        if (sortOrder.equalsIgnoreCase("desc")) {
            sort = Sort.by(Sort.Direction.DESC, sortBy);
        } else {
            sort = Sort.by(Sort.Direction.ASC, sortBy);
        }

        List<User> filteredUsers;

        if (firstName != null || lastName != null || username != null || email != null || phoneNumber != null) {
            filteredUsers = userService.findAll(firstName, lastName, username, email, phoneNumber, sort);
        } else {
            filteredUsers = userService.findAll(sort);
        }

        return filteredUsers
                .stream()
                .map(user -> this.modelMapper.map(user, UserViewDto.class))
                .collect(Collectors.toList());
    }

    /**
     * Method returning a single user.
     *
     * @param username target username
     * @return single User with username equals to the target username.
     * @throws EntityNotFoundException when User with 'target username' is not found in the DataBase.
     */
    @GetMapping("/username/{username}")
    public UserViewDto getUserByUsername(@PathVariable String username) {
        try {
            User user = userService.getByUsername(username);

            return this.modelMapper.map(user, UserViewDto.class);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Method returning a single user.
     *
     * @param id target id
     * @return single User with id equals to the target id.
     * @throws EntityNotFoundException when User with 'target id' is not found in the DataBase.
     */
    @GetMapping("/{id}")
    public UserViewDto getUserById(@PathVariable Long id) {
        try {
            User user = userService.getById(id);
            return this.modelMapper.map(user, UserViewDto.class);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Create a new user.
     *
     * @param payloadUser The user details that will use to create a new user.
     * @return The newly created entity.
     * @throws DuplicateEntityException if either username, email or phoneNumber already exist in DataBase
     */
    @PostMapping()
    public UserViewDto createUser(@RequestBody UserCreateDto payloadUser) {
        try {
            User user = this.modelMapper.map(payloadUser, User.class);
            return this.modelMapper.map(this.userService.create(user), UserViewDto.class);
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /**
     * Update user by id.
     * Update user by id, by setting all its fields to the fields of the provided payload.
     *
     * @param id          Target user id.
     * @param payloadUser The user details that will use to update the existing user (if such).
     * @param headers     Autorization key holding Username and Password
     * @return The updated user with provided target id (if such).
     * @throws AuthenticationFailureException if username or/and password are not recognized
     * @throws EntityNotFoundException        If user with specified id does not exist
     * @throws AuthorizationException         If user is not authorized to perform update operation on user with specified id
     * @throws DuplicateEntityException       if either username, email or phoneNumber already exist in DataBase
     */
    @PutMapping("/{id}")
    public UserViewDto updateUser(@PathVariable Long id,
                                  @RequestBody UserUpdateDto payloadUser,
                                  @RequestHeader HttpHeaders headers) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(headers);
            return this.modelMapper.map(this.userService.update(id, payloadUser, loggedUser), UserViewDto.class);
        } catch (AuthorizationException | AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /**
     * Logically deleting a user.
     * Changing the UserStatus property to "DELETED". Only the user itself and admin can delete a user.
     *
     * @param id      The id of the user to delete
     * @param headers Autorization key holding Username and Password
     * @throws AuthenticationFailureException if username or/and password are not recognized
     * @throws EntityNotFoundException        If user with specified id does not exist
     * @throws AuthorizationException         If user is not authorized to perform delete operation on user with specified id
     */
    @PutMapping("/{id}/delete")
    public void deleteUser(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(headers);
            this.userService.delete(id, loggedUser);
        } catch (AuthorizationException | AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Logically restoring a user.
     * Changing the UserStatus property to "ACTIVE". Only the user itself and admin can restore a user.
     *
     * @param id      The id of the user to restore
     * @param headers Autorization key holding Username and Password
     * @throws AuthenticationFailureException if username or/and password are not recognized
     * @throws EntityNotFoundException        If user with specified id does not exist
     * @throws AuthorizationException         If user is not authorized to perform delete operation on user with specified id
     */
    @PutMapping("/{id}/restore")
    public void restoreUser(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(headers);
            this.userService.restore(id, loggedUser);
        } catch (AuthenticationFailureException | AuthorizationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{id}/vehicles")
    public VehicleViewDto addVehicleInUser(@PathVariable Long id,
                                           @RequestBody VehicleCreateDto payloadVehicle,
                                           @RequestHeader HttpHeaders headers) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(headers);
            Vehicle vehicle = this.modelMapper.map(payloadVehicle, Vehicle.class);
            return this.modelMapper.map(this.userService.addVehicle(id, vehicle, loggedUser), VehicleViewDto.class);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthenticationFailureException | AuthorizationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
}
