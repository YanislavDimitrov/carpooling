package com.example.carpooling.models.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VehicleCreateDto {
    private String make;
    private String model;
    private String licencePlateNumber;
    @NotEmpty(message = "Vehicle color must not be empty")
    private String color;
    @NotEmpty(message = "Vehicle type must not be empty")
    private String type;
//    @Size(min = 4, max = 4, message = )
//    @Pattern(regexp = "[0-9]")
    private String yearOfProduction;

    public VehicleCreateDto() {
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLicencePlateNumber() {
        return licencePlateNumber;
    }

    public void setLicencePlateNumber(String licencePlateNumber) {
        this.licencePlateNumber = licencePlateNumber;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getYearOfProduction() {
        return yearOfProduction;
    }

    public void setYearOfProduction(String yearOfProduction) {
        this.yearOfProduction = yearOfProduction;
    }
}
