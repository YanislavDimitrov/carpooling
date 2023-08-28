package com.example.carpooling.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VehicleViewDto {
    private Long id;
    private String make;
    private String model;
    private String licencePlateNumber;
    private String color;
    private String yearOfProduction;
    private UserViewDto owner;
    private boolean isDeleted;
}
