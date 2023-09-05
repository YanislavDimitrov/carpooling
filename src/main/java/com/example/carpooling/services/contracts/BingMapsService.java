package com.example.carpooling.services.contracts;

public interface BingMapsService {
    String getTravelDistance(String origin, String destination);

    String getDistanceMatrixJson(String origin, String destination);

    String parseTravelDistanceAndDuration(String json);

    String getLocationJson(String address);

    String encodeURIComponent(String s);

    double[] parseCoordinates(String json);
}
