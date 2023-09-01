package com.example.carpooling.models.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FeedbackViewDto {
    @NotEmpty(message = "Creator field cannot be empty!")
    private String creator;
    @NotEmpty(message = "Travel ID cannot be empty!")
    private Long travelId;
    @NotEmpty(message = "Recipient field cannot be empty!")
    private String recipient;
    private String comment;
    private Short rating;
}
