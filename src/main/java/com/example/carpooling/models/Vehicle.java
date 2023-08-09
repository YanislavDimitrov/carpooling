package com.example.carpooling.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "vehicles")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String make;
    @Column
    private String model;
    @Column(name = "licence_plate_number")
    private String licencePlateNumber;
    @Column
    private String color;
    @Column(name = "year_of_production")
    private String yearOfProduction;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
