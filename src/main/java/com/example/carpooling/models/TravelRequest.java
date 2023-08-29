package com.example.carpooling.models;

import com.example.carpooling.models.enums.TravelRequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "travel_requests")
@Getter
@Setter
public class TravelRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
   @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User passenger;
   @JsonIgnore
   @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "travel_id")
    private Travel travel;
    @Enumerated(EnumType.STRING)
    private TravelRequestStatus status;


    public TravelRequest() {
        status = TravelRequestStatus.PENDING;
    }

}
