package com.example.carpooling.models.dtos;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FeedbackFilterDto {
    private Short rating;
    private User creator;
    private User recipient;
    private Travel travel;
    private String sortBy = "id";
    private String sortOrder = "asc";
}
