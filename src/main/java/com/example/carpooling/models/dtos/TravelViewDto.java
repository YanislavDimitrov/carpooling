package com.example.carpooling.models.dtos;

import com.example.carpooling.models.enums.TravelStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TravelViewDto {
    @NotEmpty(message = "Driver  field cannot be empty!")
    private String driverName;
    @NotEmpty(message = "Free Spots field cannot be empty!")
    private Short freeSpots;
    @NotEmpty(message = "Departure point cannot be empty!")
    private String departurePoint;
    @NotEmpty(message = "Arrival point cannot be empty!")
    private String arrivalPoint;
    @NotEmpty(message = "Departure Time cannot be empty")
    private LocalDateTime departureTime;
    private String distance;
    private String duration;
    private LocalDateTime arrivalTime;
    private String comment;
    private TravelStatus status;
    private List<TravelRequestDto> passengers;
    @Positive(message = "Price cannot be negative!")
    private String price;
}
