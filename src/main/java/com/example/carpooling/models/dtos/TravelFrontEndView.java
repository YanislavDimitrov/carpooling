package com.example.carpooling.models.dtos;

import com.example.carpooling.models.enums.TravelStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
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
    @NotNull(message = "Departure time field cannot be null!")
    private LocalDateTime departureTime;
    private String distance;
    private String duration;
    private LocalDateTime arrivalTime;
    private String comment;
    private TravelStatus status;
    private String price;
    private boolean isDeleted;
}


