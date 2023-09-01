package com.example.carpooling.models.dtos;

import com.example.carpooling.models.Vehicle;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TravelCreationOrUpdateDto {
    @NotEmpty(message = "Departure point field  cannot be empty!")
    private String departurePoint;
    @NotEmpty(message = "Arrival point field cannot be empty!")
    private String arrivalPoint;
    @Min(value = 0, message = "Free spots cannot be negative")
    @NotNull(message = "Free Spots field cannot be null!")
    private Short freeSpots;
    @NotNull(message = "The departure time field cannot be empty!")
    @FutureOrPresent(message = "The date should be valid!")
    private LocalDateTime departureTime;
    @NotNull(message = "Vehicle field cannot be null")
    private Vehicle vehicle;
    private String comment;
    @NotBlank
    @Pattern(regexp = "^(\\d{1,10}|\\d{1,10}\\$$)", message = "Price must be a non-negative number with up to 10 digits and an optional dollar sign")
    private String price;
}
