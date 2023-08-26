package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.models.Image;
import com.example.carpooling.models.User;
import com.example.carpooling.models.Vehicle;
import com.example.carpooling.models.dtos.VehicleCreateDto;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.repositories.contracts.VehicleRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vehicles")
public class VehicleMvcController {
    private final AuthenticationHelper authenticationHelper;
    private final VehicleRepository vehicleRepository;
    private final ModelMapper modelMapper;

    @ModelAttribute("isAuthenticated")
    public boolean populateIsAuthenticated(HttpSession session) {
        return session.getAttribute("currentUser") != null;
    }

    @ModelAttribute("isAdmin")
    public boolean populateIsAdmin(HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            return loggedUser.getRole() == UserRole.ADMIN;
        } catch (AuthenticationFailureException e) {
            return false;
        }
    }

    @ModelAttribute("hasProfilePicture")
    public Boolean hasProfilePicture(HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            return loggedUser.getProfilePicture() != null;
        } catch (AuthenticationFailureException e) {
            return false;
        }
    }

    @ModelAttribute("profilePicture")
    public Image populateProfilePicture(HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            return loggedUser.getProfilePicture();
        } catch (AuthenticationFailureException e) {
            return null;
        }
    }

    @Autowired
    public VehicleMvcController(AuthenticationHelper authenticationHelper, VehicleRepository vehicleRepository, ModelMapper modelMapper) {
        this.authenticationHelper = authenticationHelper;
        this.vehicleRepository = vehicleRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/create")
    public String getCreateVehicle(Model model, HttpSession session) {
        try {
            authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
        model.addAttribute("vehicle", new VehicleCreateDto());
        return "CreateVehicleView";
    }

    @PostMapping("/create")
    public String createVehicle(@Valid @ModelAttribute("vehicle") VehicleCreateDto dto,
                                BindingResult bindingResult,
                                HttpSession session) {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            return "CreateVehicleView";
        }

        Vehicle vehicleToSave = this.modelMapper.map(dto, Vehicle.class);
        vehicleToSave.setOwner(loggedUser);
        this.vehicleRepository.save(vehicleToSave);

        return String.format("redirect:/users/%d", loggedUser.getId());
    }
}
