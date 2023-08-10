package com.example.carpooling.models.dtos;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

public class FeedbackViewDto {
   @NotEmpty(message = "Creator field cannot be empty!")
    private String creator;
   @NotEmpty(message = "Travel ID cannot be empty!")
    private Long id;
    private String comment;

    private Short rating;

    public FeedbackViewDto() {
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Short getRating() {
        return rating;
    }

    public void setRating(Short rating) {
        this.rating = rating;
    }
}
