package com.example.carpooling.models.dtos;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;

public class TravelUpdateDto {
    @NotEmpty(message = "Please,fill this field!")
    private String departurePoint;

    @NotEmpty(message = "Please,fill this field!")
    private String arrivalPoint;
    @NotEmpty(message = "Please,fill this field!")
    private Short freeSpots;
    @NotEmpty(message = "Please,fill this field!")
    private LocalDateTime departureTime;
    private String comment;

    public TravelUpdateDto() {
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
