package com.example.carpooling.controllers.rest;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.UserViewDto;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/users")
public class UserRestController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserRestController(UserRepository userRepository, UserService userService, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    /**
     * Method returning a Collection of Users that suit a set of filters and search options.
     * <p>
     * Filter and search options are not required and if no such are specified the method will work as a regular "getAll" method.
     *
     * @param firstName   element that will filter the users with the provided firstName, if specified (null if not specified).
     * @param lastName    element that will filter the users with the provided lastName, if specified (null if not specified).
     * @param username    element that will filter the users with the provided username, if specified (null if not specified).
     * @param email       element that will filter the users with the provided email, if specified (null if not specified).
     * @param phoneNumber element that will filter the users with the provided phoneNumber, if specified (null if not specified).
     * @param sortBy      specifies the "sort by" parameter.
     * @param sortOrder   specifies the sort order (Descending or Ascending).
     * @return List of users that suit all provided set of filtering parameters.
     */
    @GetMapping()
    public List<UserViewDto> getAll(
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
            filteredUsers = userRepository.findByCriteria(firstName, lastName, username, email, phoneNumber, sort);
        } else {
            filteredUsers = userRepository.findAll(sort);
        }

        return filteredUsers.stream().map(user -> {
            UserViewDto dto = this.modelMapper.map(user, UserViewDto.class);
            dto.setFullName(String.format("%s %s", user.getFirstName(), user.getLastName()));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Method returning a single user.
     *
     * @param username target username
     * @return single User with username equals to the target username.
     *
     * @throws EntityNotFoundException when User with 'target username' is not found in the DataBase.
     */
    @GetMapping("/username/{username}")
    public UserViewDto getByUsername(@PathVariable String username) {
        try {
            User user = userService.getByUsername(username);
            UserViewDto userViewDto = this.modelMapper.map(user, UserViewDto.class);
            userViewDto.setFullName(String.format("%s %s", user.getFirstName(), user.getLastName()));
            return userViewDto;
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    /**
     * Method returning a single user.
     *
     * @param id target id
     * @return single User with id equals to the target id.
     *
     * @throws EntityNotFoundException when User with 'target id' is not found in the DataBase.
     */
    @GetMapping("/{id}")
    public UserViewDto getById(@PathVariable Long id) {
        try {
            User user = userService.getById(id);
            UserViewDto userViewDto = this.modelMapper.map(user, UserViewDto.class);
            userViewDto.setFullName(String.format("%s %s", user.getFirstName(), user.getLastName()));
            return userViewDto;
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
