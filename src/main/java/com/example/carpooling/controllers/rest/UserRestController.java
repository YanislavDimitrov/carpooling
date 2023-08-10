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
    public static final String USER_NOT_FOUND = "User with username %s was not found!";
    private final UserRepository userRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserRestController(UserRepository userRepository, UserService userService, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

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

    @GetMapping("/{username}")
    public UserViewDto getByUsername(@PathVariable String username) {
        try {
            User user = userService.getByUsername(username);
            UserViewDto userViewDto = this.modelMapper.map(user, UserViewDto.class);
            userViewDto.setFullName(String.format("%s %s", user.getFirstName(), user.getLastName()));
            return userViewDto;
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(USER_NOT_FOUND, username));
        }
    }
}
