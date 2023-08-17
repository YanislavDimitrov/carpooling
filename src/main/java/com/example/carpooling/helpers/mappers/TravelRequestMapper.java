package com.example.carpooling.helpers.mappers;

import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.dtos.TravelRequestDto;
import com.example.carpooling.models.dtos.TravelRequestViewDto;
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

    public TravelRequestViewDto toViewDto(TravelRequest travelRequest) {
        TravelRequestViewDto travelRequestViewDto = new TravelRequestViewDto();
        travelRequestViewDto.setDriverName(String.format("%s %s",
                travelRequest.getTravel().getDriver().getFirstName(),
                travelRequest.getTravel().getDriver().getLastName()));
        travelRequestViewDto.setPassengerName(String.format("%s %s",
                travelRequest.getPassenger().getFirstName(),
                travelRequest.getPassenger().getLastName()));
        travelRequestViewDto.setDepartureTime(travelRequest.getTravel().getDepartureTime().toString());
        travelRequestViewDto.setDeparturePoint(travelRequest.getTravel().getDeparturePoint());
        travelRequestViewDto.setArrivalPoint(travelRequest.getTravel().getArrivalPoint());
        return travelRequestViewDto;
    }
}
