package com.example.carpooling.services.contracts;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;

import java.util.List;

public interface TravelRequestService {
    List<TravelRequest> get();
    TravelRequest get(Long id);
    TravelRequest createRequest(Travel travel, User user);
    void update(TravelRequest travelRequest, User editor);
    void delete(Long id,User editor);
    void approveRequest(Long id);
    void rejectRequest(Long id);

}