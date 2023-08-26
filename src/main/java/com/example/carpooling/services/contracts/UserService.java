package com.example.carpooling.services.contracts;

import com.example.carpooling.models.User;
import com.example.carpooling.models.Vehicle;
import com.example.carpooling.models.dtos.UserUpdateDto;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.List;

public interface UserService {
    List<User> findAll(Sort sort);

    List<User> findAll(String firstName, String lastName, String username, String email, String phoneNumber, Sort sort);
    List<User> getAll();

    User getById(Long id);

    User getByUsername(String username);

    User create(User user) throws MessagingException, IOException;

    void delete(Long id, User loggedUser);

    Long count();

    void restore(Long id, User loggedUser);

    User update(Long id, UserUpdateDto payloadUser, User loggedUser);

    Vehicle addVehicle(Long id, Vehicle payloadVehicle, User loggedUser);

    List<Vehicle> getVehiclesByUserId(Long id, User loggedUser);

    void block(Long id, User loggedUser);

    void unblock(Long id, User loggedUser);

    void upgrade(Long id, User loggedUser);

    void downgrade(Long id, User loggedUser);

    void unverify(Long id);
    void verify(Long id);
}
