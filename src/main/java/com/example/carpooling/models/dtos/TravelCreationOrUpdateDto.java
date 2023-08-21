package com.example.carpooling.models.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class TravelCreationOrUpdateDto {
    @NotEmpty(message = "Departure point field  cannot be empty!")
    private String departurePoint;
    @NotEmpty(message = "Arrival point field cannot be empty!")
    private String arrivalPoint;
    @Min(value = 0, message = "Free spots cannot be negative")
    private Short freeSpots;
    @FutureOrPresent(message = "The date should be valid!")
    private LocalDateTime departureTime;
    private String comment;


    public TravelCreationOrUpdateDto() {
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
