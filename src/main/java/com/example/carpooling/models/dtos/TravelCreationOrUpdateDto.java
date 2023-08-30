package com.example.carpooling.models.dtos;

import com.example.carpooling.models.Vehicle;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class TravelCreationOrUpdateDto {
    @NotEmpty(message = "Departure point field  cannot be empty!")
    private String departurePoint;
    @NotEmpty(message = "Arrival point field cannot be empty!")
    private String arrivalPoint;
    @Min(value = 1, message = "Free spots cannot be negative")
    @NotNull(message = "Free Spots field cannot be null!")
    private Short freeSpots;
    @NotNull(message = "The departure time field cannot be empty!")
    @FutureOrPresent(message = "The date should be valid!")
    private LocalDateTime departureTime;
    @NotNull(message = "Vehicle field cannot be null")
    private Vehicle vehicle;
    private String comment;
    @Positive(message = "Price cannot be negative!")
    private String price;


    public TravelCreationOrUpdateDto() {
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getDeparturePoint() {
        return departurePoint;
    }

    public void setDeparturePoint(String departurePoint) {
        this.departurePoint = departurePoint;
    }

    public String getArrivalPoint() {
        return arrivalPoint;
    }

    public void setArrivalPoint(String arrivalPoint) {
        this.arrivalPoint = arrivalPoint;
    }

    public Short getFreeSpots() {
        return freeSpots;
    }

    public void setFreeSpots(Short freeSpots) {
        this.freeSpots = freeSpots;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
