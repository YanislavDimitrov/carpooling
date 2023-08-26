package com.example.carpooling.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class UserUpdateDto {
    private Long id;
    @Size(min = 2, max = 20, message = "Firstname must be between 2 and 20 symbols.")
    private String firstName;
    @Size(min = 2, max = 20, message = "Lastname must be between 2 and 20 symbols.")
    private String lastName;
    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 symbols.")
    private String userName;
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Incorrect mail format.")
    private String email;
    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 symbols.")
    private String phoneNumber;

    public UserUpdateDto() {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
