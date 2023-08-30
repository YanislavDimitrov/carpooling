package com.example.carpooling.helpers;

import com.example.carpooling.models.Travel;
import com.example.carpooling.services.contracts.BingMapsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class CalculationDistanceDurationHelper {
    private final BingMapsService bingMapsService;

    @Autowired
    public CalculationDistanceDurationHelper(BingMapsService bingMapsService) {
        this.bingMapsService = bingMapsService;
    }

    public List<String> calculateDistanceAndDuration(Travel travel) {
        String departurePoint = bingMapsService.getLocationJson(travel.getDeparturePoint());
        double[] coordinatesOfDeparturePoint = bingMapsService.parseCoordinates(departurePoint);
        double departureLatitude = coordinatesOfDeparturePoint[0];
        double departureLongitude = coordinatesOfDeparturePoint[1];

        String arrivalPoint = bingMapsService.getLocationJson(travel.getArrivalPoint());
        double[] coordinatesOfArrivalPoint = bingMapsService.parseCoordinates(arrivalPoint);
        double arrivalLatitude = coordinatesOfArrivalPoint[0];
        double arrivalLongitude = coordinatesOfArrivalPoint[1];

        String departurePointInCoordinates = String.format("%f,%f", departureLatitude, departureLongitude);
        String arrivalPointInCoordinates = String.format("%f,%f", arrivalLatitude, arrivalLongitude);

        int intervalBetweenDurationAndDistance = bingMapsService.getTravelDistance(
                departurePointInCoordinates, arrivalPointInCoordinates).indexOf('m');

        List<String> distanceAndDuration = new ArrayList<>();
        String duration = bingMapsService.getTravelDistance(departurePointInCoordinates, arrivalPointInCoordinates)
                .substring(intervalBetweenDurationAndDistance + 1);
        distanceAndDuration.add(duration);

        String distance = bingMapsService.getTravelDistance(departurePointInCoordinates, arrivalPointInCoordinates)
                .substring(0, intervalBetweenDurationAndDistance + 1);
        distanceAndDuration.add(distance);

        return distanceAndDuration;

    }

    public LocalDateTime calculateEstimatedTimeOfArrival(Travel travel) {
        int indexOfMinutes = travel.getTravelDuration().indexOf('m');
        double minutesToAdd = Double.parseDouble(travel.getTravelDuration().substring(0, indexOfMinutes - 1));
        long secondsToAdd = (long) (minutesToAdd * 60);
        return travel.getDepartureTime().plusSeconds(secondsToAdd);
    }

}
