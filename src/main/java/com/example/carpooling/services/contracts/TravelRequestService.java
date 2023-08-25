package com.example.carpooling.services.contracts;

import com.example.carpooling.models.Travel;
import com.example.carpooling.models.TravelRequest;
import com.example.carpooling.models.User;
import com.example.carpooling.models.enums.TravelRequestStatus;

import java.util.List;

public interface TravelRequestService {
    List<TravelRequest> get();
    TravelRequest get(Long id);
    List<TravelRequest> getPending();
    List<TravelRequest> getByTravel(Travel travel);
    TravelRequest findByTravelIsAndPassengerIsAndStatus(Travel travel , User passenger , TravelRequestStatus status);
    void createRequest(Travel travel, User user);
    void update(TravelRequest travelRequest, User editor);
    void delete(Long id,User editor);
    void approveRequest(Travel travel, User editor , User requestCreator);
    void rejectRequest(Travel travel , User editor , User requestCreator);
    void rejectRequestWhenUserIsAlreadyPassenger(Travel travel  , User user , User editor);
    void rejectWhenAlreadyPassenger(Long id , User editor , Travel travel);
    boolean existsTravelRequestByTravelAndPassengerAndStatus(Travel travel, User user, TravelRequestStatus status);

}
