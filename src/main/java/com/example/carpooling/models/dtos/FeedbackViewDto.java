package com.example.carpooling.models.dtos;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

public class FeedbackViewDto {
   @NotEmpty(message = "Creator field cannot be empty!")
    private String creator;
   @NotEmpty(message = "Travel ID cannot be empty!")
    private Long travelId;
   @NotEmpty(message = "Recipient field cannot be empty!")
   private String recipient;
    private String comment;

    private Short rating;

    public FeedbackViewDto() {
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Long getTravelId() {
        return travelId;
    }

    public void setTravelId(Long travelId) {
        this.travelId = travelId;
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
