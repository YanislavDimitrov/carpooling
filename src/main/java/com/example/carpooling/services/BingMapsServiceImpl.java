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
/**
 * A service class that interacts with Bing Maps API to retrieve location information, calculate travel distances and durations.
 *
 * This class provides methods for working with Bing Maps API to perform location-based operations, including obtaining location data,
 * calculating travel distances and durations, and parsing coordinates. It serves as an interface between the application and Bing Maps services.
 * The class utilizes a predefined API key for authentication and provides error handling for various scenarios, such as invalid locations or
 * impossible travel routes.
 *
 * @author Ivan Boev
 * @version 1.0
 * @since 2023-09-04
 */
@Service
public class BingMapsServiceImpl implements BingMapsService {

    // API Key for access of the Microsoft Bing Maps Endpoints
    private static final String API_KEY = "ApCDqrWiyt1uxxpCrXxFDT44JTvyUnba2onqQ9NEyrYFEKCq5F9-U02xEb2rcMcw";

    // Constant messages for errors
    public static final String INVALID_LOCAtiON = "This location is not valid!";
    public static final String IMPOSSIBLE_TRAVEL = "We are supporting only travels which can be done by land , this travel needs sea/air transport!";

    //Microsoft Bing Maps URL
    public static final String MICROSOFT_URL = "http://dev.virtualearth.net/REST/v1/Locations?q=";
    public static final String KEY = "&key=";
    public static final String GET = "GET";

    /**
     * Calculates the travel distance between two locations.
     *
     * @param origin      The starting location coordinates.
     * @param destination The destination location coordinates.
     * @return A string representation of travel distance and duration (e.g., "4.2 km, 7 minutes").
     */
    public String getTravelDistance(String origin, String destination) {
        String json = getDistanceMatrixJson(origin, destination);
        return parseTravelDistanceAndDuration(json);
    }

    /**
     * Retrieves the JSON response for a distance matrix between two locations.
     *
     * @param origin      The starting location coordinates.
     * @param destination The destination location coordinates.
     * @return The JSON response containing distance matrix data.
     */
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

    /**
     * Reads data from the provided HTTP connection and returns the response as a string.
     *
     * This method reads data from an established HTTP connection and processes the response, returning it as a string. It is typically used
     * to retrieve data from a web service endpoint. The method handles HTTP response codes, and if the response code is not HTTP_OK (200),
     * it logs an error message indicating the failure.
     *
     * @param connection The established HTTP connection to read data from.
     * @return The response data as a string if the HTTP request is successful (HTTP_OK); otherwise, null.
     * @throws IOException if an error occurs while reading data from the connection.
     */
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

    /**
     * Parses the JSON response to extract travel distance and duration.
     *
     * @param json The JSON response containing distance matrix data.
     * @return A string representation of travel distance and duration (e.g., "4.2 km, 7 minutes").
     * @throws InvalidTravelException if the travel route is impossible or invalid.
     */
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

    /**
     * Retrieves location information in JSON format for a given address.
     *
     * @param address The address for which location information is requested.
     * @return The JSON response containing location data.
     */
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

    /**
     * Encodes a string for use in a URL.
     *
     * @param s The string to be encoded.
     * @return The encoded string.
     */
    public String encodeURIComponent(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /**
     * Parses coordinates (latitude and longitude) from location JSON data.
     *
     * @param json The JSON response containing location data.
     * @return An array of latitude and longitude values as doubles.
     * @throws InvalidLocationException if the location data is invalid or not found.
     */
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
