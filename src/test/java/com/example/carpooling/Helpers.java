package com.example.carpooling;

import com.example.carpooling.models.*;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.models.enums.UserStatus;
import com.example.carpooling.models.enums.VehicleType;

import java.time.LocalDateTime;

public class Helpers {

    public static User createMockUser() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirstName("MockFirstName");
        mockUser.setLastName("MockLastName");
        mockUser.setUserName("MockUserName");
        mockUser.setEmail("mock@user.com");
        mockUser.setPassword("MockPassword");
        mockUser.setPhoneNumber("1234567890");
        mockUser.setProfilePicture(createMockImage());
        mockUser.setRole(UserRole.USER);
        mockUser.setStatus(UserStatus.ACTIVE);
        mockUser.setValidated(true);
        return mockUser;
    }

    public static Vehicle createMockVehicle() {
        Vehicle mockVehicle = new Vehicle();
        mockVehicle.setId(1L);
        mockVehicle.setModel("MockModel");
        mockVehicle.setMake("MockMake");
        mockVehicle.setColor("MockColor");
        mockVehicle.setLicencePlateNumber("MockLicencePlateNumber");
        mockVehicle.setYearOfProduction("MockYear");
        mockVehicle.setType(VehicleType.SALOON);
        mockVehicle.setOwner(createMockUser());
        mockVehicle.setDeleted(false);
        return mockVehicle;
    }

    public static Image createMockImage() {
        Image mockImage = new Image();
        mockImage.setId(1L);
        mockImage.setImageUrl("MockURL");
        return mockImage;
    }

    public static Travel createMockActiveTravel() {
        Travel mockTravel = new Travel();
        mockTravel.setId(1L);
        mockTravel.setComment("MockComment");
        mockTravel.setStatus(TravelStatus.ACTIVE);
        mockTravel.setTravelDuration("MockDuration");
        mockTravel.setArrivalPoint("MockArrivalPoint");
        mockTravel.setDeparturePoint("MockDeparturePoint");
        mockTravel.setDepartureTime(LocalDateTime.now());
        mockTravel.setEstimatedTimeOfArrival(LocalDateTime.now());
        mockTravel.setDeleted(false);
        mockTravel.setDistance("MockDistance");
        mockTravel.setDriver(createMockUser());
        mockTravel.setFreeSpots((short) 2);
        mockTravel.setPrice("MockPrice");
        mockTravel.setVehicle(createMockVehicle());
        return mockTravel;
    }

    public static Feedback createMockFeedback() {
        Feedback mockFeedback = new Feedback();
        User mockUser = createMockUser();
        mockFeedback.setId(1L);
        mockFeedback.setCreator(mockUser);
        mockFeedback.setComment("MockComment");
        mockFeedback.setRating((short) 2);
        mockFeedback.setRecipient(mockUser);
        mockFeedback.setTravel(createMockActiveTravel());
        mockFeedback.setDeleted(false);
        return mockFeedback;
    }
}
