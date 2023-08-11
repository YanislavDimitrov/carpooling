package com.example.carpooling.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class BingMapsService {
    private static final String API_KEY = "ApCDqrWiyt1uxxpCrXxFDT44JTvyUnba2onqQ9NEyrYFEKCq5F9-U02xEb2rcMcw";

    public String getTravelDistance(String origin, String destination) {
        String json = getDistanceMatrixJson(origin, destination);
        return parseTravelDistanceAndDuration(json);
    }

    private String getDistanceMatrixJson(String origin, String destination) {
        String url = "https://dev.virtualearth.net/REST/v1/Routes/DistanceMatrix?origins=" +
                origin + "&destinations=" + destination +
                "&travelMode=driving&key=" + API_KEY;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } else {
                System.out.println("API request failed with response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String parseTravelDistanceAndDuration(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        double travelDistance = jsonObject
                .getAsJsonArray("resourceSets").get(0)
                .getAsJsonObject().getAsJsonArray("resources").get(0)
                .getAsJsonObject().getAsJsonArray("results").get(0)
                .getAsJsonObject().get("travelDistance").getAsDouble();
        double travelDuration = jsonObject
                .getAsJsonArray("resourceSets").get(0)
                .getAsJsonObject().getAsJsonArray("resources").get(0)
                .getAsJsonObject().getAsJsonArray("results").get(0)
                .getAsJsonObject().get("travelDuration").getAsDouble();

        return "Travel Distance: " + travelDistance + " km, Travel Duration: " + travelDuration + " minutes";
    }




    public String getLocationJson(String address) {
        String encodedAddress = encodeURIComponent(address);
        String url = "http://dev.virtualearth.net/REST/v1/Locations?q=" +
                encodedAddress + "&key=" + API_KEY;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } else {
                System.out.println("API request failed with response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String encodeURIComponent(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding URI component: " + e.getMessage());
        }
    }
    public double[] parseCoordinates(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        JsonObject point = jsonObject
                .getAsJsonArray("resourceSets").get(0)
                .getAsJsonObject().getAsJsonArray("resources").get(0)
                .getAsJsonObject().getAsJsonObject("point");

        double latitude = point.getAsJsonArray("coordinates").get(0).getAsDouble();
        double longitude = point.getAsJsonArray("coordinates").get(1).getAsDouble();

        return new double[]{latitude, longitude};
    }

}
