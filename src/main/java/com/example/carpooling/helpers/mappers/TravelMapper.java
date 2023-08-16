package com.example.carpooling.helpers.mappers;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.dtos.TravelCreationOrUpdateDto;
import com.example.carpooling.models.dtos.TravelUpdateDto;
import com.example.carpooling.models.dtos.TravelViewDto;
import com.example.carpooling.models.enums.TravelRequestStatus;
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
        int wholeNumberIndex = travel.getTravelDuration().indexOf(" ");
        double durationAsDouble = Double.parseDouble(travel.getTravelDuration().substring(0, wholeNumberIndex));
        int hours = (int) (durationAsDouble / 60);
        double minutes = durationAsDouble - hours * 60;
        if (hours > 0 && minutes > 0) {
            travelViewDto.setDuration(String.format("%d hours and %.2f minutes", hours, minutes));
        }
        if (hours > 0 && minutes == 0) {
            travelViewDto.setDuration(String.format("%d hours", hours));
        }
        if (hours < 0 && minutes > 0) {
            travelViewDto.setDuration(String.format("%.2f minutes", minutes));
        }
        travelViewDto.setArrivalTime(travel.getEstimatedTimeOfArrival());

//        travelViewDto.setRequests(travel.
//                getTravelRequests()
//                .stream()
//                .map(travelRequestMapper::toDto)
//                .collect(Collectors.toList()));
        travelViewDto.setPassengers(travel
                .getTravelRequests()
                .stream()
                .filter(travelRequest -> travelRequest.getStatus() == TravelRequestStatus.APPROVED)
                .map(travelRequestMapper::toDto)
                .toList());
        return travelViewDto;
    }

    public Travel toTravelFromTravelCreationDto(TravelCreationOrUpdateDto travelCreationOrUpdateDto) {
        Travel travel = new Travel();
        travel.setStatus(TravelStatus.ACTIVE);
        travel.setArrivalPoint(travelCreationOrUpdateDto.getArrivalPoint());
        travel.setDeparturePoint(travelCreationOrUpdateDto.getDeparturePoint());
        travel.setDepartureTime(travelCreationOrUpdateDto.getDepartureTime());
        travel.setComment(travelCreationOrUpdateDto.getComment());
        travel.setFreeSpots(travelCreationOrUpdateDto.getFreeSpots());
        return travel;
    }

    public Travel toTravelFromTravelUpdateSaveDto(Travel travel, TravelCreationOrUpdateDto travelUpdateDto) {
        travel.setArrivalPoint(travelUpdateDto.getArrivalPoint());
        travel.setDeparturePoint(travelUpdateDto.getDeparturePoint());
        travel.setComment(travelUpdateDto.getComment());
        travel.setDepartureTime(travelUpdateDto.getDepartureTime());
        travel.setFreeSpots(travelUpdateDto.getFreeSpots());
        return travel;
    }
}
