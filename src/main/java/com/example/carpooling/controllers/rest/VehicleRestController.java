package com.example.carpooling.controllers.rest;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.DuplicateEntityException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.UserUpdateDto;
import com.example.carpooling.models.dtos.UserViewDto;
import com.example.carpooling.models.dtos.VehicleUpdateDto;
import com.example.carpooling.models.dtos.VehicleViewDto;
import com.example.carpooling.services.contracts.VehicleService;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

    @Autowired
    public VehicleRestController(AuthenticationHelper authenticationHelper, VehicleService vehicleService, ModelMapper modelMapper) {
        this.authenticationHelper = authenticationHelper;
        this.vehicleService = vehicleService;
        this.modelMapper = modelMapper;
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

    @PutMapping("/{id}")
    public VehicleViewDto updateVehicle(@PathVariable Long id,
                                        @RequestBody VehicleUpdateDto payloadVehicle,
                                        @RequestHeader HttpHeaders headers) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(headers);
            return this.modelMapper.map(this.vehicleService.update(id, payloadVehicle, loggedUser), VehicleViewDto.class);
        } catch (AuthorizationException | AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
