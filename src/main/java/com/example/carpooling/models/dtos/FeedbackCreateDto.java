package com.example.carpooling.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FeedbackCreateDto {
    private String comment;
    private Short rating;
}
