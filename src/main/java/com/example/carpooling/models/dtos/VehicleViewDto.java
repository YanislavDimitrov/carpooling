package com.example.carpooling.models.dtos;

import com.example.carpooling.models.User;

public class VehicleViewDto {
    private Long id;
    private String make;
    private String model;
    private String licencePlateNumber;
    private String color;
    private String yearOfProduction;
    private UserViewDto owner;

    public VehicleViewDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getYearOfProduction() {
        return yearOfProduction;
    }

    public void setYearOfProduction(String yearOfProduction) {
        this.yearOfProduction = yearOfProduction;
    }

    public UserViewDto getOwner() {
        return owner;
    }

    public void setOwner(UserViewDto owner) {
        this.owner = owner;
    }
}
