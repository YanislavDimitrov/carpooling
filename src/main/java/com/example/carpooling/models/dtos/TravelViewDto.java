package com.example.carpooling.models.dtos;

import com.example.carpooling.models.Coordinate;
import com.example.carpooling.models.enums.TravelStatus;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;

public class TravelViewDto {
    @NotEmpty(message = "Driver  field cannot be empty!")
    private String driverName;
    @NotEmpty(message = "Vehicle field cannot be empty!")
    private String vehicle;
    @NotEmpty(message = "Free Spots field cannot be empty!")
    private Short freeSpots;
    @NotEmpty(message = "Departure Time cannot be empty")
    private LocalDateTime departureTime;

    private String comment;
    @NotEmpty(message = "Starting point cannot be empty!")
    private Coordinate startPoint;
    @NotEmpty(message = "End Point field cannot be empty!")
    private Coordinate endPoint;
    @NotEmpty(message = "Travel Status cannot be empty!")
    private TravelStatus status;

    public TravelViewDto() {
    }

    public Coordinate getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Coordinate startPoint) {
        this.startPoint = startPoint;
    }

    public Coordinate getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Coordinate endPoint) {
        this.endPoint = endPoint;
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

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public TravelStatus getStatus() {
        return status;
    }

    public void setStatus(TravelStatus status) {
        this.status = status;
    }
}
