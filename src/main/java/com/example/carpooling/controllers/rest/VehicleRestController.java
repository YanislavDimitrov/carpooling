package com.example.carpooling.controllers.rest;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.models.User;
import com.example.carpooling.services.contracts.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping("api/vehicles")
@RestController
public class VehicleRestController {
    private final AuthenticationHelper authenticationHelper;
    private final VehicleService vehicleService;
    @Autowired
    public VehicleRestController(AuthenticationHelper authenticationHelper, VehicleService vehicleService) {
        this.authenticationHelper = authenticationHelper;
        this.vehicleService = vehicleService;
    }

    @PutMapping("{id}/delete")
    public void deleteVehicle(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(headers);
            this.vehicleService.delete(id, loggedUser);
        } catch (AuthorizationException | AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
