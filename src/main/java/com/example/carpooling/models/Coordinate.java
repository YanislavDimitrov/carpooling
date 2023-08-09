package com.example.carpooling.models;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Coordinate {
    private Double longitude;
    private Double latitude;
}
