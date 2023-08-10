package com.example.carpooling.helpers;

import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.UserViewDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserViewDto toViewDto(User user) {
        UserViewDto dto = new UserViewDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUserName(user.getUserName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        return dto;
    }

}
