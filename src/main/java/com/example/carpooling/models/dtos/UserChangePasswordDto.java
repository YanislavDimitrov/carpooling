package com.example.carpooling.models.dtos;

import jakarta.validation.constraints.Pattern;

public class UserChangePasswordDto {

    private String oldPassword;
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&*!])[A-Za-z\\d@#$%^&*!]{8,}$", message = "Password must be at least 8 symbols and should contain capital letter, digit and special symbol.")
    private String newPassword;

    private String confirmNewPassword;

    public UserChangePasswordDto() {
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}
