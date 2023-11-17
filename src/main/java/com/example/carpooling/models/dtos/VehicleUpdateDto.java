package com.example.carpooling.models.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VehicleUpdateDto {
    private Long id;
    @NotEmpty(message = "Vehicle make must not be empty")
    private String make;
    @NotEmpty(message = "Vehicle model must not be empty")
    private String model;
    private String licencePlateNumber;
    @NotEmpty(message = "Vehicle color must not be empty")
    private String color;
    @NotEmpty(message = "Vehicle type must not be empty")
    private String type;
    @Pattern(regexp = "^(19\\d\\d|20[0-9]{2})$", message = "Production year must be between 1900 and 2099")
    private String yearOfProduction;
}
