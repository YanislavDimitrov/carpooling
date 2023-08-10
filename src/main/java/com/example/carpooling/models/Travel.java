package com.example.carpooling.models;

import com.example.carpooling.models.enums.TravelStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Table(name = "travels")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Travel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "start_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "start_longitude"))
    })
    private Coordinate startPoint;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "end_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "end_longitude"))
    })
    private Coordinate endpoint;
    @Column(name = "free_spots")
    private short freeSpots;
    @Column(name = "departure_time")
    private LocalDateTime departureTime;
    @Column(name = "comment")
    private String comment;
    @ManyToOne
    private User driver;
    @ManyToOne
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    private TravelStatus status;


}
