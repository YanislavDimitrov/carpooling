package com.example.carpooling.services;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.services.contracts.TravelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TravelServiceImpl implements TravelService {

    private final TravelRepository travelRepository;

    public TravelServiceImpl(TravelRepository travelRepository) {
        this.travelRepository = travelRepository;
    }


    public List<Travel> get() {
        return travelRepository.findAll();
    }

    @Override
    public Travel getById(Long id) {
    Optional<Travel> travel = travelRepository.findById(id);
        if(travel.isPresent()) {
            return travel.get();
         } else {
            throw new EntityNotFoundException(
                    String.format("Travel with ID %d is not existing!",id)
            );
        }
    }

    @Override
    public List<Travel> getByDriver(User user) {
    return  null;
    }

    @Override
    public List<Travel> getByStatus(TravelStatus status) {
        return null;
    }

    @Override
    public List<Travel> getByFreeSpots(int freeSpots) {
        return null;
    }

    @Override
    public void create(Travel travel) {

    }

    @Override
    public void update(Travel travel) {

    }

    @Override
    public void delete(Travel travel) {

    }

    @Override
    public void completeTravel(Travel travel) {

    }

    @Override
    public void cancelTravel(Travel travel) {

    }

}
