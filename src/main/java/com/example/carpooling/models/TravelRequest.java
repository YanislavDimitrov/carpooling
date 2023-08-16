package com.example.carpooling.models;

import com.example.carpooling.models.enums.TravelRequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "travel_requests")
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPassenger() {
        return passenger;
    }

    public void setPassenger(User passenger) {
        this.passenger = passenger;
    }

    public Travel getTravel() {
        return travel;
    }

    public void setTravel(Travel travel) {
        this.travel = travel;
    }

    public TravelRequestStatus getStatus() {
        return status;
    }

    public void setStatus(TravelRequestStatus status) {
        this.status = status;
    }
}
