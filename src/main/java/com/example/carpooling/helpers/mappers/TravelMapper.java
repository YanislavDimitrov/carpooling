package com.example.carpooling.helpers.mappers;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.dtos.TravelViewDto;
import com.example.carpooling.services.contracts.TravelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TravelMapper {

    private final TravelService travelService;

    @Autowired
    public TravelMapper(TravelService travelService) {
        this.travelService = travelService;
    }

    public TravelViewDto fromTravel(Travel travel) {
        TravelViewDto travelViewDto = new TravelViewDto();
        travelViewDto.setVehicle(String.format("%s %s",travel.getVehicle().getMake(),travel.getVehicle().getModel()));
        travelViewDto.setComment(travel.getComment());
        travelViewDto.setStatus(travel.getStatus());
        travelViewDto.setDriverName(
                String.format("%s %s",travel.getDriver().getFirstName(),travel.getDriver().getLastName())
        );
        travelViewDto.setDepartureTime(travel.getDepartureTime());
        travelViewDto.setEndPoint(travel.getEndpoint());
        travelViewDto.setFreeSpots(travel.getFreeSpots());
        travelViewDto.setStartPoint(travel.getStartPoint());
        return travelViewDto;
    }
}
