package com.example.carpooling.services.contracts;

import com.example.carpooling.models.User;
import com.example.carpooling.models.Vehicle;

public interface VehicleService {
    void delete(Long id, User loggedUser);
}
