package com.example.carpooling.helpers.mappers;

import com.example.carpooling.helpers.CalculationDistanceDurationHelper;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.dtos.TravelCreationOrUpdateDto;
import com.example.carpooling.models.dtos.TravelFrontEndView;
import com.example.carpooling.models.dtos.TravelViewDto;
import com.example.carpooling.models.enums.TravelRequestStatus;
import com.example.carpooling.services.BingMapsServiceImpl;
import com.example.carpooling.services.contracts.TravelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TravelMapper {

    private final TravelService travelService;
    private final TravelRequestMapper travelRequestMapper;
    private final BingMapsServiceImpl bingMapsService;
    private final CalculationDistanceDurationHelper calculationHelper;

    @Autowired
    public TravelMapper(TravelService travelService, TravelRequestMapper travelRequestMapper, BingMapsServiceImpl bingMapsService, CalculationDistanceDurationHelper calculationHelper) {
        this.travelService = travelService;
        this.travelRequestMapper = travelRequestMapper;
        this.bingMapsService = bingMapsService;
        this.calculationHelper = calculationHelper;
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
        travelViewDto.setPrice(travel.getPrice());
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
        travel.setPrice(travelCreationOrUpdateDto.getPrice());
        List<String> distanceAndDuration = calculationHelper.calculateDistanceAndDuration(travel);
        travel.setTravelDuration(distanceAndDuration.get(0));
        travel.setDistance(distanceAndDuration.get(1));
        travel.setEstimatedTimeOfArrival(calculationHelper.calculateEstimatedTimeOfArrival(travel));
        return travel;
    }

    public Travel toTravelFromTravelUpdateSaveDto(Travel travel, TravelCreationOrUpdateDto travelUpdateDto) {
        travel.setArrivalPoint(travelUpdateDto.getArrivalPoint());
        travel.setDeparturePoint(travelUpdateDto.getDeparturePoint());
        travel.setComment(travelUpdateDto.getComment());
        travel.setDepartureTime(travelUpdateDto.getDepartureTime());
        travel.setFreeSpots(travelUpdateDto.getFreeSpots());
        travel.setPrice(travelUpdateDto.getPrice());
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
        travelCreationOrUpdateDto.setPrice(travel.getPrice());
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
        travelFrontEndView.setPrice(travel.getPrice());
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
            double minutes = durationAsDouble;
            if (minutes > 0) {
                travelFrontEndView.setDuration(String.format("%.0f minutes", minutes));
            }
        }
        travelFrontEndView.setDeparturePoint(travel.getDeparturePoint());
        travelFrontEndView.setStatus(travel.getStatus());
        travelFrontEndView.setFreeSpots(travel.getFreeSpots());
        travelFrontEndView.setDepartureTime(travel.getDepartureTime());
        travelFrontEndView.setDriverUsername(travel.getDriver().getUserName());
        travelFrontEndView.setDriverName(String.format("%s %s", travel.getDriver().getFirstName(), travel.getDriver().getLastName()));
        return travelFrontEndView;
    }

    public Travel fromTravelRequest(TravelRequest travelRequest) {
        Travel travel = new Travel();
        travel = travelRequest.getTravel();
        return travel;
    }

    public Travel fromTravelCreateToTestDepartureTime(TravelCreationOrUpdateDto travelCreationOrUpdateDto) {
        Travel travel = new Travel();
        travel.setDepartureTime(travelCreationOrUpdateDto.getDepartureTime());
        travel.setDeparturePoint(travelCreationOrUpdateDto.getDeparturePoint());
        travel.setArrivalPoint(travelCreationOrUpdateDto.getArrivalPoint());
        travel.setComment(travelCreationOrUpdateDto.getComment());
        travel.setFreeSpots(travelCreationOrUpdateDto.getFreeSpots());
        travel.setVehicle(travelCreationOrUpdateDto.getVehicle());
        travel.setPrice(travelCreationOrUpdateDto.getPrice());
        return travel;
    }
}
