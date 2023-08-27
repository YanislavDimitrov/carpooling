package com.example.carpooling.helpers.mappers;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.dtos.*;
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
        int hours;
        if (durationAsDouble >= 60) {
            hours = (int) (durationAsDouble / 60);
            double minutes = durationAsDouble - hours * 60;
            if (hours > 0 && minutes > 0) {
                travelViewDto.setDuration(String.format("%d hours %.0f minutes", hours, minutes));
            }
            if (hours > 0 && minutes == 0) {
                travelViewDto.setDuration(String.format("%d hours", hours));
            }
        } else {
            hours = 0;
            double minutes = durationAsDouble;
            if (minutes > 0) {
                travelViewDto.setDuration(String.format("%.0f minutes", minutes));
            }
        }
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
        travel.setArrivalPoint(travelCreationOrUpdateDto.getArrivalPoint());
        travel.setDeparturePoint(travelCreationOrUpdateDto.getDeparturePoint());
        travel.setDepartureTime(travelCreationOrUpdateDto.getDepartureTime());
        travel.setComment(travelCreationOrUpdateDto.getComment());
        travel.setFreeSpots(travelCreationOrUpdateDto.getFreeSpots());
        travel.setVehicle(travelCreationOrUpdateDto.getVehicle());
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

    public TravelCreationOrUpdateDto fromTravel(Long id) {
        TravelCreationOrUpdateDto travelCreationOrUpdateDto = new TravelCreationOrUpdateDto();
        Travel travel = travelService.getById(id);
        travelCreationOrUpdateDto.setArrivalPoint(travel.getArrivalPoint());
        travelCreationOrUpdateDto.setComment(travel.getComment());
        travelCreationOrUpdateDto.setDeparturePoint(travel.getDeparturePoint());
        travelCreationOrUpdateDto.setFreeSpots(travel.getFreeSpots());
        travelCreationOrUpdateDto.setDepartureTime(travel.getDepartureTime());
        return travelCreationOrUpdateDto;
    }

    public TravelFrontEndView fromTravelToFrontEnd(Travel travel) {
        TravelFrontEndView travelFrontEndView = new TravelFrontEndView();
        travelFrontEndView.setId(travel.getId());
        travelFrontEndView.setArrivalPoint(travel.getArrivalPoint());
        travelFrontEndView.setArrivalTime(travel.getEstimatedTimeOfArrival());
        travelFrontEndView.setComment(travel.getComment());
        travelFrontEndView.setDistance(travel.getDistance());
        travelFrontEndView.setDeleted(travel.isDeleted());
        int wholeNumberIndex = travel.getTravelDuration().indexOf(" ");
        double durationAsDouble = Double.parseDouble(travel.getTravelDuration().substring(0, wholeNumberIndex));
        int hours;
        if (durationAsDouble >= 60) {
            hours = (int) (durationAsDouble / 60);
            double minutes = durationAsDouble - hours * 60;
            if (hours > 0 && minutes > 0) {
                travelFrontEndView.setDuration(String.format("%d hours %.0f minutes", hours, minutes));
            }
            if (hours > 0 && minutes == 0) {
                travelFrontEndView.setDuration(String.format("%d hours", hours));
            }

        } else {
            hours = 0;
            double minutes = durationAsDouble;
            if (minutes > 0) {
                travelFrontEndView.setDuration(String.format("%.0f minutes", minutes));
            }
        }
        travelFrontEndView.setDeparturePoint(travel.getDeparturePoint());
        travelFrontEndView.setStatus(travel.getStatus());
        travelFrontEndView.setFreeSpots(travel.getFreeSpots());
        travelFrontEndView.setDepartureTime(travel.getDepartureTime());
        travelFrontEndView.setDriverName(travel.getDriver().getUserName());
        return travelFrontEndView;
    }

    public Travel fromTravelRequest(TravelRequest travelRequest) {
        Travel travel = new Travel();
        travel = travelRequest.getTravel();
        return travel;
    }

}
