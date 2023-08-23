package com.example.carpooling.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterDto {
    @Size(min = 2, max = 20, message = "Firstname must be between 2 and 20 symbols.")
    private String firstName;
    @Size(min = 2, max = 20, message = "Lastname must be between 2 and 20 symbols.")
    private String lastName;
    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 symbols.")
    private String userName;
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&*!])[A-Za-z\\d@#$%^&*!]{8,}$", message = "Password must be at least 8 symbols and should contain capital letter, digit and special symbol.")
    private String password;
    private String confirmPassword;
    @Email
    private String email;
    @Size(min = 10,max = 10)
    private String phoneNumber;

    public RegisterDto() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
