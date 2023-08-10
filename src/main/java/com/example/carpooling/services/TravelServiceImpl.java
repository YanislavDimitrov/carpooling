package com.example.carpooling.services;

import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.repositories.contracts.TravelRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.TravelService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TravelServiceImpl implements TravelService {

    public static final String TRAVEL_NOT_FOUND = "Travel with ID %d is not existing!";
    public static final String USER_NOT_FOUND = "User with ID %d does not exist!";
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;

    public TravelServiceImpl(TravelRepository travelRepository, UserRepository userRepository) {
        this.travelRepository = travelRepository;
        this.userRepository = userRepository;
    }


    public List<Travel> get() {
        return travelRepository.findAll();
    }

    @Override
    public Travel getById(Long id) {
        return travelRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TRAVEL_NOT_FOUND, id)));
    }

    @Override
    public List<Travel> getByDriver(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND, id)));
        return travelRepository.findAllByDriverIs(user);
    }

    @Override
    public List<Travel> findByCriteria(String driver, TravelStatus status, Short freeSpots, LocalDateTime departureTime, Sort sort) {
        return travelRepository.findByCriteria(driver, status, freeSpots, departureTime, sort);
    }

    public List<Travel> findAll(Sort sort) {
        return travelRepository.findAll(sort);
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
    public void delete(Long id) {
        travelRepository.delete(id);
    }

    @Override
    public void completeTravel(Long id) {

        travelRepository.completeTravel(id);
    }

    @Override
    public void cancelTravel(Long id) {
        travelRepository.delete(id);
    }

}
