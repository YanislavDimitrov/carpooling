package com.example.carpooling.models.dtos;

import com.example.carpooling.models.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FeedbackFilterDto {
    private short rating;
    private User creator;
    private User recipient;
    private String sortBy = "id";
    private String sortOrder = "asc";
}
