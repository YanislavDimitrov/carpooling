package com.example.carpooling.models;

public class Coordinates {
    @Embedded
    @AttributeOverrides( {
            @AttributeOverride(name = "latitude", column = @Column(name = "start_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "start_longitude"))
    })
    private Coordinates startPoint;
}
