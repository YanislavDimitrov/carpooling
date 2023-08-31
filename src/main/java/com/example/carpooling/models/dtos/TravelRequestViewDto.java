package com.example.carpooling.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TravelRequestViewDto {
    private String passengerName ;
    private String departurePoint;
    private String arrivalPoint;
    private String departureTime;
    private String driverName;
}
