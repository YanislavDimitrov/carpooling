package com.example.carpooling.services;

import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.User;
import com.example.carpooling.models.Vehicle;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.repositories.contracts.VehicleRepository;
import com.example.carpooling.services.contracts.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.example.carpooling.helpers.CustomMessages.DELETE_USER_AUTHORIZATION_MESSAGE;
import static com.example.carpooling.helpers.CustomMessages.DELETE_VEHICLE_AUTHORIZATION_MESSAGE;

@Service
public class VehicleServiceImpl implements VehicleService {
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Autowired
    public VehicleServiceImpl(UserRepository userRepository, VehicleRepository vehicleRepository) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional
    public void delete(Long id, User loggedUser) {
        Optional<Vehicle> optionalVehicle = this.vehicleRepository.findById(id);

        if (optionalVehicle.isEmpty()) {
            throw new EntityNotFoundException("Vehicle", id);
        }

        if (isAdmin(loggedUser) || isSameUser(loggedUser, optionalVehicle.get().getOwner())) {
            this.vehicleRepository.delete(id);
        } else {
            throw new AuthorizationException(
                    String.format(DELETE_VEHICLE_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    private boolean isAdmin(User loggedUser) {
        return loggedUser.getRole().equals(UserRole.ADMIN);
    }

    private static boolean isSameUser(User loggedUser, User targetUser) {
        return targetUser.getUserName().equals(loggedUser.getUserName());
    }
}