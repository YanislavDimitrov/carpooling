package com.example.carpooling.services;

import com.example.carpooling.exceptions.InvalidLocationException;
import com.example.carpooling.exceptions.InvalidTravelException;
import com.example.carpooling.services.contracts.BingMapsService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class BingMapsServiceImpl implements BingMapsService {
    private static final String API_KEY = "ApCDqrWiyt1uxxpCrXxFDT44JTvyUnba2onqQ9NEyrYFEKCq5F9-U02xEb2rcMcw";
    public static final String INVALID_LOCAtiON = "This location is not valid!";
    public static final String IMPOSSIBLE_TRAVEL = "We are supporting only travels which can be done by land , this travel needs sea/air transport!";
    public static final String MICROSOFT_URL = "http://dev.virtualearth.net/REST/v1/Locations?q=";
    public static final String KEY = "&key=";
    public static final String GET = "GET";

    public String getTravelDistance(String origin, String destination) {
        String json = getDistanceMatrixJson(origin, destination);
        return parseTravelDistanceAndDuration(json);
    }

    public String getDistanceMatrixJson(String origin, String destination) {
        String url = "https://dev.virtualearth.net/REST/v1/Routes/DistanceMatrix?origins=" +
                origin + "&destinations=" + destination +
                "&travelMode=driving&key=" + API_KEY;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            String response = readTheDataFromEndPoint(connection);
            if (response != null) return response;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String readTheDataFromEndPoint(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                return response.toString();
            }
        } else {
            System.out.println("API request failed with response code: " + responseCode);
        }
        return null;
    }

    public String parseTravelDistanceAndDuration(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        double travelDistance = jsonObject
                .getAsJsonArray("resourceSets").get(0)
                .getAsJsonObject().getAsJsonArray("resources").get(0)
                .getAsJsonObject().getAsJsonArray("results").get(0)
                .getAsJsonObject().get("travelDistance").getAsDouble();

        if (travelDistance == -1) {
            throw new InvalidTravelException(IMPOSSIBLE_TRAVEL);
        }
        double travelDuration = jsonObject
                .getAsJsonArray("resourceSets").get(0)
                .getAsJsonObject().getAsJsonArray("resources").get(0)
                .getAsJsonObject().getAsJsonArray("results").get(0)
                .getAsJsonObject().get("travelDuration").getAsDouble();
        if (travelDuration == -1) {
            throw new InvalidTravelException(IMPOSSIBLE_TRAVEL);
        }
        return travelDistance + " km" + travelDuration + " minutes";
    }


    public String getLocationJson(String address) {
        String encodedAddress = encodeURIComponent(address);
        String url = MICROSOFT_URL +
                encodedAddress + KEY + API_KEY;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(GET);

            String response = readTheDataFromEndPoint(connection);
            if (response != null) return response;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String encodeURIComponent(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public double[] parseCoordinates(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        JsonArray resourcesArray = jsonObject
                .getAsJsonArray("resourceSets").get(0)
                .getAsJsonObject().getAsJsonArray("resources");
        if (resourcesArray.size() == 0) {
            throw new InvalidLocationException(INVALID_LOCAtiON);
        }
        JsonObject point = resourcesArray.get(0)
                .getAsJsonObject().getAsJsonObject("point");


        double latitude = point.getAsJsonArray("coordinates").get(0).getAsDouble();
        double longitude = point.getAsJsonArray("coordinates").get(1).getAsDouble();

        return new double[]{latitude, longitude};
    }

}
