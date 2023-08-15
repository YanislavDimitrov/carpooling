package com.example.carpooling.models.dtos;

public class FeedbackCreateDto {

    private String comment ;
    private Short rating ;

    public FeedbackCreateDto() {
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
