package com.example.carpooling.models.dtos;

import com.example.carpooling.models.Image;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.Vehicle;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.models.enums.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserViewDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private UserStatus status;
    private boolean isValidated;
    private Image profilePicture;
    private List<VehicleViewDto> vehicles;
}
