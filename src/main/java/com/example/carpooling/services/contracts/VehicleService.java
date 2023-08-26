package com.example.carpooling.services.contracts;

import com.example.carpooling.models.User;
import com.example.carpooling.models.Vehicle;
import com.example.carpooling.models.dtos.VehicleUpdateDto;

public interface VehicleService {
    void delete(Long id, User loggedUser);

    Vehicle update(Long id, VehicleUpdateDto payloadVehicle, User loggedUser);

    Vehicle getById(Long id);

}
