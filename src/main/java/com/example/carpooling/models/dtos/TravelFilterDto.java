package com.example.carpooling.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TravelFilterDto {
    private short freeSpots;
    private LocalDate departedAfter;
    private LocalDate departedBefore;
    private String departurePoint;
    private String arrivalPoint;
    private String price;
    private String sortBy = "id";
    private String sortOrder = "asc";
}
