package com.example.carpooling.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FeedbackFilterDto {
    private short rating;
    private String creator;
    private String recipient;
    private String sortBy = "id";
    private String sortOrder = "asc";
}
