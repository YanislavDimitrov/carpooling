package com.example.carpooling.helpers.mappers;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.Vehicle;
import com.example.carpooling.models.dtos.TravelCreationDto;
import com.example.carpooling.models.dtos.TravelViewDto;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.services.contracts.TravelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TravelMapper {

    private final TravelService travelService;
    private final TravelRequestMapper travelRequestMapper;

    @Autowired
    public TravelMapper(TravelService travelService, TravelRequestMapper travelRequestMapper) {
        this.travelService = travelService;
        this.travelRequestMapper = travelRequestMapper;
    }

    public TravelViewDto fromTravel(Travel travel) {
        TravelViewDto travelViewDto = new TravelViewDto();
//        travelViewDto.setVehicle(String.format("%s %s", travel.getVehicle().getMake(), travel.getVehicle().getModel()));
        travelViewDto.setComment(travel.getComment());
        travelViewDto.setStatus(travel.getStatus());
        travelViewDto.setDriverName(
                String.format("%s %s", travel.getDriver().getFirstName(), travel.getDriver().getLastName())
        );
        travelViewDto.setDepartureTime(travel.getDepartureTime());
        travelViewDto.setArrivalPoint(travel.getArrivalPoint());
        travelViewDto.setDeparturePoint(travel.getDeparturePoint());
        travelViewDto.setFreeSpots(travel.getFreeSpots());
        travelViewDto.setDistance(travel.getDistance());
        travelViewDto.setRequests(travel.
                getTravelRequests()
                .stream()
                .map(travelRequestMapper::toDto)
                .collect(Collectors.toList()));
        return travelViewDto;
    }

    public Travel toTravelFromTravelCreationDto(TravelCreationDto travelCreationDto) {
        Travel travel = new Travel();
        travel.setStatus(TravelStatus.ACTIVE);
        travel.setArrivalPoint(travelCreationDto.getArrivalPoint());
        travel.setDeparturePoint(travelCreationDto.getDeparturePoint());
        travel.setDepartureTime(travelCreationDto.getDepartureTime());
        travel.setComment(travelCreationDto.getComment());
        travel.setFreeSpots(travelCreationDto.getFreeSpots());
        return travel;
    }
}
