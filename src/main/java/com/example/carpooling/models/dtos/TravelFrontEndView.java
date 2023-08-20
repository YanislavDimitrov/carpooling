package com.example.carpooling.models.dtos;

import com.example.carpooling.models.enums.TravelStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class TravelFrontEndView {
    private Long id;

    @NotEmpty(message = "Driver  field cannot be empty!")
    private String driverName;
    @NotNull(message = "Free spots cannot be null")
    @Min(value = 0, message = "Free spots cannot be negative")
    private Short freeSpots;
    @NotEmpty(message = "Departure point cannot be empty!")
    private String departurePoint;
    @NotEmpty(message = "Arrival point cannot be empty!")
    private String arrivalPoint;
    @FutureOrPresent(message = "The date should be valid!")
    private LocalDateTime departureTime;
    private String distance;
    private String duration;
    private LocalDateTime arrivalTime;
    private String comment;
    private TravelStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
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

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public TravelStatus getStatus() {
        return status;
    }

    public void setStatus(TravelStatus status) {
        this.status = status;
    }
}


