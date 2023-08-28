package com.example.carpooling.models.dtos;

import com.example.carpooling.models.Image;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.models.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserPreviewDto {
    private Long id;
    private String fullName;
    private String userName;
    private String email;
    private String phoneNumber;
    private Image profilePicture;
    private UserRole role;
    private UserStatus status;
    private boolean isValidated;

    public UserPreviewDto(Long id,
                          String fullName,
                          String userName,
                          String email,
                          String phoneNumber,
                          Image profilePicture,
                          UserRole role,
                          UserStatus status,
                          boolean isValidated) {
        this.id = id;
        this.fullName = fullName;
        this.userName = userName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePicture = profilePicture;
        this.role = role;
        this.status = status;
        this.isValidated = isValidated;
    }

    public boolean isAdmin() {
        return this.role.equals(UserRole.ADMIN);
    }
}
