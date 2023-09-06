package com.example.carpooling.services;

import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.User;
import com.example.carpooling.models.Vehicle;
import com.example.carpooling.models.dtos.VehicleUpdateDto;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.repositories.contracts.VehicleRepository;
import com.example.carpooling.services.contracts.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.example.carpooling.helpers.ConstantMessages.DELETE_VEHICLE_AUTHORIZATION_MESSAGE;
import static com.example.carpooling.helpers.ConstantMessages.UPDATE_VEHICLE_AUTHORIZATION_MESSAGE;

/**
 * The {@code VehicleServiceImpl} class provides services related to vehicle management.
 * It allows users to perform actions such as updating, deleting, and retrieving vehicles.
 * This class ensures proper authorization checks before performing vehicle-related actions.
 *
 * @author Yanislav Dimitrov & Ivan Boev
 * @version 1.0
 * @since 06.09.23
 */
@Service
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;

    /**
     * Constructs a new {@code VehicleServiceImpl} with the specified dependencies.
     *
     * @param vehicleRepository The repository for managing vehicle data.
     */
    @Autowired
    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Deletes a vehicle with the given ID, subject to authorization checks.
     *
     * @param id         The ID of the vehicle to delete.
     * @param loggedUser The user attempting the deletion.
     * @throws EntityNotFoundException If the vehicle with the given ID does not exist.
     * @throws AuthorizationException  If the user is not authorized to delete the vehicle.
     */
    @Override
    @Transactional
    public void delete(Long id, User loggedUser) {
        Optional<Vehicle> optionalVehicle = this.vehicleRepository.findById(id);

        if (optionalVehicle.isEmpty()) {
            throw new EntityNotFoundException("Vehicle", id);
        }

        if (isAdmin(loggedUser) || areSameUser(loggedUser, optionalVehicle.get().getOwner())) {
            this.vehicleRepository.delete(id);
        } else {
            throw new AuthorizationException(
                    String.format(DELETE_VEHICLE_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    /**
     * Updates a vehicle's details with the given information, subject to authorization checks.
     *
     * @param id             The ID of the vehicle to update.
     * @param payloadVehicle The updated vehicle information.
     * @param loggedUser     The user attempting the update.
     * @return The updated vehicle.
     * @throws EntityNotFoundException If the vehicle with the given ID does not exist.
     * @throws AuthorizationException  If the user is not authorized to update the vehicle.
     */
    @Override
    public Vehicle update(Long id, VehicleUpdateDto payloadVehicle, User loggedUser) {
        Optional<Vehicle> optionalTargetVehicle = this.vehicleRepository.findById(id);

        if (optionalTargetVehicle.isEmpty()) {
            throw new EntityNotFoundException("Vehicle", id);
        }

        Vehicle targetVehicle = optionalTargetVehicle.get();
        User owner = targetVehicle.getOwner();
        if (areSameUser(loggedUser, owner)) {

            targetVehicle.setMake(payloadVehicle.getMake());
            targetVehicle.setModel(payloadVehicle.getModel());
            targetVehicle.setLicencePlateNumber(payloadVehicle.getLicencePlateNumber());
            targetVehicle.setColor(payloadVehicle.getColor());
            targetVehicle.setYearOfProduction(payloadVehicle.getYearOfProduction());

            return this.vehicleRepository.save(targetVehicle);
        } else {
            throw new AuthorizationException(
                    String.format(UPDATE_VEHICLE_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    /**
     * Retrieves a vehicle by its ID.
     *
     * @param id The ID of the vehicle to retrieve.
     * @return The retrieved vehicle.
     * @throws EntityNotFoundException If the vehicle with the given ID does not exist.
     */
    @Override
    public Vehicle getById(Long id) {
        Optional<Vehicle> optionalVehicle = this.vehicleRepository.findById(id);
        if (optionalVehicle.isEmpty()) {
            throw new EntityNotFoundException("Vehicle");
        }
        return optionalVehicle.get();
    }

    /**
     * Checks if a user has administrator (admin) privileges.
     *
     * @param loggedUser The user to check for admin privileges.
     * @return {@code true} if the user has admin privileges, {@code false} otherwise.
     */
    private boolean isAdmin(User loggedUser) {
        return loggedUser.getRole().equals(UserRole.ADMIN);
    }

    /**
     * Checks if two users are the same user based on their usernames.
     *
     * @param loggedUser The first user.
     * @param targetUser The second user.
     * @return {@code true} if the users are the same, {@code false} otherwise.
     */
    private static boolean areSameUser(User loggedUser, User targetUser) {
        return targetUser.getUserName().equals(loggedUser.getUserName());
    }
}
