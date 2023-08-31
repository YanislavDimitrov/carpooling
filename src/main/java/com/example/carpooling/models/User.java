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
@Getter
@Setter
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

    public void addVehicle(Vehicle vehicle) {
        this.vehicles.add(vehicle);
    }

    public Long getVehiclesCount() {
        return this.vehicles.stream().filter(v -> !v.isDeleted()).count();
    }

    public boolean isAdmin() {
        return this.role.equals(UserRole.ADMIN);
    }
}
