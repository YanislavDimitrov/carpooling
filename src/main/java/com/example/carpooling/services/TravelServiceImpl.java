package com.example.carpooling.services;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.services.contracts.TravelService;
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
        if (travel.isPresent()) {
            return travel.get();
        } else {
            throw new EntityNotFoundException(
                    String.format("Travel with ID %d is not existing!", id)
            );
        }
    }

    @Override
    public List<Travel> getByDriver(User user) {
        return travelRepository.findAllByDriverIs(user);
    }

    @Override
    public List<Travel> getByStatus(TravelStatus status) {
        return travelRepository.findAllByStatus(status);
    }

    @Override
    public List<Travel> getByFreeSpots(int freeSpots) {
        return travelRepository.findAllByFreeSpots(freeSpots);
    }

    @Override
    public void create(Travel travel) {
        travelRepository.save(travel);
    }

    @Override
    public void update(Long id) {
        Travel travel = getById(id);
        travelRepository.save(travel);
    }

    @Override
    public void delete(Long id ) {
        travelRepository.delete(id);
    }

    @Override
    public void completeTravel(Long id) {

        travelRepository.completeTravel(id);
    }

    @Override
    public void cancelTravel(Long id ) {
        travelRepository.delete(id);
    }

}
