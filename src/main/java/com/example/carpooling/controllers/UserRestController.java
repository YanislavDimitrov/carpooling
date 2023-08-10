package com.example.carpooling.controllers;

import com.example.carpooling.helpers.UserMapper;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.UserViewDto;
import com.example.carpooling.repositories.contracts.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/users")
public class UserRestController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserRestController(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
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

        return filteredUsers.stream().map(userMapper::toViewDto).collect(Collectors.toList());
    }
}
