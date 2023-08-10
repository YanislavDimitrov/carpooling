package com.example.carpooling.controllers.rest;

import com.example.carpooling.models.Travel;
import com.example.carpooling.services.contracts.TravelService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/travel")
public class TravelRestController {

    private final TravelService travelService;
    private final ModelMapper modelMapper;
@Autowired
    public TravelRestController(TravelService travelService, ModelMapper modelMapper) {
        this.travelService = travelService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public List<Travel> getAll() {
        return travelService.get();
    }
}
