package com.example.carpooling.helpers.mappers;

import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.dtos.TravelRequestDto;
import com.example.carpooling.services.contracts.TravelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TravelRequestMapper {
    private final TravelService travelService;
@Autowired
    public TravelRequestMapper(TravelService travelService) {
        this.travelService = travelService;
    }

    public TravelRequestDto toDto(TravelRequest travelRequest) {
    TravelRequestDto travelRequestDto = new TravelRequestDto();
    travelRequestDto.setUsername(travelRequest.getPassenger().getUserName());
    return travelRequestDto;
    }
}
