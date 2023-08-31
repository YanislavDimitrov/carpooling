package com.example.carpooling.models.dtos;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
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
}
