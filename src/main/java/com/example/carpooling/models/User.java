package com.example.carpooling.models;

import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.models.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Table(name = "users")
@Entity
//@Getter
//@Setter
//@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "username", unique = true)
    private String userName;
    @Column
    private String password;
    @Column(unique = true)
    private String email;
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @Column(name = "is_validated")
    private boolean isValidated;
    @OneToOne
    @JoinColumn(name = "image_id")
    private Image profilePicture;
    @JsonIgnore
    @OneToMany(mappedBy = "driver")
    private List<Travel> travelsAsDriver;
    @JsonIgnore
    @OneToMany(mappedBy = "passenger")
    private List<TravelRequest> travelsAsPassenger;
    @JsonIgnore
    @OneToMany(mappedBy = "recipient")
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "owner")
    private List<Vehicle> vehicles;

    public User() {
        travelsAsPassenger = new ArrayList<>();
        travelsAsDriver = new ArrayList<>();
        feedbacks = new ArrayList<>();
        vehicles = new ArrayList<>();
        this.role = UserRole.USER;
        this.status = UserStatus.ACTIVE;
    }

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public List<Travel> getTravelsAsDriver() {
        return travelsAsDriver;
    }

    public void setTravelsAsDriver(List<Travel> travelsAsDriver) {
        this.travelsAsDriver = travelsAsDriver;
    }

    public List<TravelRequest> getTravelsAsPassenger() {
        return travelsAsPassenger;
    }

    public void setTravelsAsPassenger(List<TravelRequest> travelsAsPassenger) {
        this.travelsAsPassenger = travelsAsPassenger;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public boolean isValidated() {
        return isValidated;
    }

    public void setValidated(boolean validated) {
        isValidated = validated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userName, user.userName) && Objects.equals(email, user.email) && Objects.equals(phoneNumber, user.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, email, phoneNumber);
    }

    public Image getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Image profilePicture) {
        this.profilePicture = profilePicture;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public void addVehicle(Vehicle vehicle) {
        this.vehicles.add(vehicle);
    }
}
