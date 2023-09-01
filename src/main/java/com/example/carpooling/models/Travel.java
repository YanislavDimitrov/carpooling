package com.example.carpooling.models;

import com.example.carpooling.models.enums.TravelStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "travels")
@Entity
@Getter
@Setter
public class Travel {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",
            updatable = false)
    private Long id;
    @Column(name = "departure_point")
    private String departurePoint;
    @Column(name = "arrival_point")
    private String arrivalPoint;
    @Column(name = "free_spots")
    private Short freeSpots;
    @Column(name = "departure_time")
    private LocalDateTime departureTime;
    @Column(name = "comment")
    private String comment;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    private User driver;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    private TravelStatus status;

    @Column(name = "distance")
    private String distance;
    @Column(name = "duration")
    private String travelDuration;
    @Column(name = "arrival_time")
    private LocalDateTime estimatedTimeOfArrival;

    @OneToMany(mappedBy = "travel", fetch = FetchType.EAGER)
    private List<TravelRequest> travelRequests;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "price")
    private String price;

    public Travel() {
        travelRequests = new ArrayList<>();
        status = TravelStatus.PLANNED;
    }
}
