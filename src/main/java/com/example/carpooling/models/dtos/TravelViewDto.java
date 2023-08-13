package com.example.carpooling.models.dtos;

import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.enums.TravelStatus;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.List;

public class TravelViewDto {
    @NotEmpty(message = "Driver  field cannot be empty!")
    private String driverName;
    @NotEmpty(message = "Vehicle field cannot be empty!")
    private String vehicle;
    @NotEmpty(message = "Free Spots field cannot be empty!")
    private Short freeSpots;
    @NotEmpty(message = "Departure point cannot be empty!")
    private String departurePoint;
    @NotEmpty(message = "Arrival point cannot be empty!")
    private String arrivalPoint;
    @NotEmpty(message = "Departure Time cannot be empty")
    private LocalDateTime departureTime;

    private String distance ;
    private String comment;

    private TravelStatus status;
    private List<TravelRequestDto> requests;

    public TravelViewDto() {
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public List<TravelRequestDto> getRequests() {
        return requests;
    }

    public void setRequests(List<TravelRequestDto> requests) {
        this.requests = requests;
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
