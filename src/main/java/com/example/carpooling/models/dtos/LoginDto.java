package com.example.carpooling.models.dtos;

import jakarta.validation.constraints.NotEmpty;

public class LoginDto {
    @NotEmpty(message = "Username cannot be empty!")
    private String userName;
    @NotEmpty(message = "Password cannot be empty!")
    private String password;

    public LoginDto() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
