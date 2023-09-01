package com.example.carpooling.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "feedbacks")
@Entity(name = "Feedback")
@Getter
@Setter
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id")
    private User creator;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipient_id")
    private User recipient;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "travel_id")
    private Travel travel;
    @Column(name = "comment")
    private String comment;
    @Column(name = "rating")
    private Short rating;
    @Column(name = "is_deleted")
    private boolean isDeleted;

    public Feedback() {
        isDeleted = false;
    }

}
