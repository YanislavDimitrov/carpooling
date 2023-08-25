package com.example.carpooling.services;

import com.example.carpooling.exceptions.*;
import com.example.carpooling.exceptions.duplicate.DuplicateEmailException;
import com.example.carpooling.exceptions.duplicate.DuplicatePhoneNumberException;
import com.example.carpooling.exceptions.duplicate.DuplicateUsernameException;
import com.example.carpooling.models.*;
import com.example.carpooling.models.dtos.UserUpdateDto;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.repositories.contracts.TokenRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.repositories.contracts.VehicleRepository;
import com.example.carpooling.services.contracts.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.example.carpooling.helpers.CustomMessages.*;

@Service
public class UserServiceImpl implements UserService {

    public static final String CONFIRMATION_EMAIL_TEMPLATE_PATH = "src/main/resources/templates/WelcomeTemplateEmail.html";
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final TokenRepository tokenRepository;
    private final JavaMailSender javaMailSender;


    @Autowired
    public UserServiceImpl(UserRepository userRepository, VehicleRepository vehicleRepository, TokenRepository tokenRepository, JavaMailSender javaMailSender) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.tokenRepository = tokenRepository;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public List<User> findAll(Sort sort) {
        return this.userRepository.findAll();
    }

    @Override
    public List<User> findAll(String firstName, String lastName, String username, String email, String phoneNumber, Sort sort) {
        return this.userRepository.findByCriteria(firstName, lastName, username, email, phoneNumber, sort);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        Optional<User> optionalUser = this.userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new EntityNotFoundException("User");
        }
        return optionalUser.get();
    }

    @Override
    public User getByUsername(String username) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new EntityNotFoundException("User", "username", username);
        }
        return user;
    }


    @Override
    public User create(User user) throws MessagingException, IOException {
        checkForDuplicateUser(user);
        this.userRepository.save(user);

        VerificationToken verificationToken = new VerificationToken(user);
        tokenRepository.save(verificationToken);
        String verificationLink = "http://localhost:8080/verify?token=" + verificationToken.getToken();
        String htmlContent = readHtmlFromFile();
        htmlContent = htmlContent.replace("verificationLinkPlaceholder", verificationLink);
        sendVerificationEmail(user.getEmail(), htmlContent);


        return user;
    }

    private void sendVerificationEmail(String emailAddress, String content) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        ClassPathResource imageResource = new ClassPathResource("src/main/resources/static/images/CoverPicture.svg");
        helper.addInline("image123", imageResource);

        helper.setFrom("carpoolingalpha@gmail.com");
        helper.setTo(emailAddress);
        helper.setSubject("Carpool Email Verification");
        helper.setText(content, true);

        javaMailSender.send(message);
    }

    private static String readHtmlFromFile() throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIRMATION_EMAIL_TEMPLATE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    @Override
    public User update(Long id, UserUpdateDto payloadUser, User loggedUser) {
        Optional<User> optionalTargetUser = this.userRepository.findById(id);

        if (optionalTargetUser.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        User targetUser = optionalTargetUser.get();
        if (isAdmin(loggedUser) || areSameUser(loggedUser, targetUser)) {

            targetUser.setFirstName(payloadUser.getFirstName());
            targetUser.setLastName(payloadUser.getLastName());
            targetUser.setEmail(payloadUser.getEmail());
            targetUser.setPhoneNumber(payloadUser.getPhoneNumber());
            targetUser.setUserName(payloadUser.getUserName());

            checkForDuplicateUser(targetUser);
            return this.userRepository.save(targetUser);
        } else {
            throw new AuthorizationException(
                    String.format(UPDATE_USER_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }


    @Override
    @Transactional
    public void delete(Long id, User loggedUser) {

        Optional<User> optionalUserToDelete = this.userRepository.findById(id);

        if (optionalUserToDelete.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        User userToDelete = optionalUserToDelete.get();
        if (areSameUser(loggedUser, userToDelete)) {
            this.userRepository.delete(id);
            deleteUserFeedbacks(userToDelete);
            deleteUserTravels(userToDelete);
        } else {
            throw new AuthorizationException(
                    String.format(DELETE_USER_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    @Override
    @Transactional
    public void restore(Long id, User loggedUser) {
        Optional<User> optionalUserToRestore = this.userRepository.findById(id);

        if (optionalUserToRestore.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        if (areSameUser(loggedUser, optionalUserToRestore.get())) {
            this.userRepository.restore(id);
        } else {
            throw new AuthorizationException(
                    String.format(DELETE_USER_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    @Transactional
    @Override
    public void block(Long id, User loggedUser) {
        Optional<User> optionalUserToBlock = this.userRepository.findById(id);

        if (optionalUserToBlock.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        User userToBlock = optionalUserToBlock.get();

        if (isAdmin(loggedUser) && !areSameUser(loggedUser, userToBlock)) {
            this.userRepository.block(id);
            deleteUserFeedbacks(userToBlock);
            deleteUserTravels(userToBlock);
        } else {
            throw new AuthorizationException(
                    String.format(BLOCK_USER_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }


    @Transactional
    @Override
    public void unblock(Long id, User loggedUser) {
        Optional<User> optionalUserToUnblock = this.userRepository.findById(id);

        if (optionalUserToUnblock.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        User userToUnlock = optionalUserToUnblock.get();

        if (isAdmin(loggedUser)) {
            this.userRepository.restore(id);
            recoverUserFeedbacks(userToUnlock);
            recoverUserTravels(userToUnlock);
        } else {
            throw new AuthorizationException(
                    String.format(UNBLOCK_USER_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    @Override
    @Transactional
    public void upgrade(Long id, User loggedUser) {
        if (!loggedUser.getRole().equals(UserRole.ADMIN)) {
            throw new AuthorizationException(String.format(UPGRADE_USER_AUTHORIZATION_MESSAGE,
                    loggedUser.getUserName(),
                    id));
        }
        Optional<User> optionalTargetUser = this.userRepository.findById(id);

        if (optionalTargetUser.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        this.userRepository.upgrade(id);
    }

    @Override
    @Transactional
    public void downgrade(Long id, User loggedUser) {
        if (!loggedUser.getRole().equals(UserRole.ADMIN)) {
            throw new AuthorizationException(String.format(DOWNGRADE_USER_AUTHORIZATION_MESSAGE,
                    loggedUser.getUserName(),
                    id));
        }
        Optional<User> optionalTargetUser = this.userRepository.findById(id);

        if (optionalTargetUser.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }
        this.userRepository.downgrade(id);
    }

    @Override
    @Transactional
    public void validate(Long id) {
        this.userRepository.validate(id);
    }


    @Override
    public List<Vehicle> getVehiclesByUserId(Long id, User loggedUser) {
        Optional<User> optionalTargetUser = this.userRepository.findById(id);

        if (optionalTargetUser.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }

        if (isAdmin(loggedUser) || areSameUser(loggedUser, optionalTargetUser.get())) {
            return this.vehicleRepository.findAllByOwnerId(id);
        } else {
            throw new AuthorizationException(
                    String.format(GET_VEHICLES_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    @Override
    public Vehicle addVehicle(Long id, Vehicle payloadVehicle, User loggedUser) {
        Optional<User> optionalTargetUser = this.userRepository.findById(id);

        if (optionalTargetUser.isEmpty()) {
            throw new EntityNotFoundException("User", id);
        }
        User targetUser = optionalTargetUser.get();
        if (isAdmin(loggedUser) || areSameUser(loggedUser, targetUser)) {
            payloadVehicle.setOwner(targetUser);
            targetUser.addVehicle(payloadVehicle);
            this.userRepository.save(targetUser);
            return this.vehicleRepository.save(payloadVehicle);
        } else {
            throw new AuthorizationException(
                    String.format(CREATE_VEHICLE_AUTHORIZATION_MESSAGE
                            , loggedUser.getUserName()
                            , id));
        }
    }

    public Long count() {
        return this.userRepository.count();
    }

    private void checkForDuplicateUser(User user) {
        User userWithUserName =
                this.userRepository.findByUserName(user.getUserName());
        if (userWithUserName != null) {
            if (!userWithUserName.getId().equals(user.getId())) {
                throw new DuplicateUsernameException("User", user.getUserName());
            }
        }
        User userWithEmail =
                this.userRepository.findByEmail(user.getEmail());
        if (userWithEmail != null) {
            if (!userWithEmail.getId().equals(user.getId())) {
                throw new DuplicateEmailException("User", user.getEmail());
            }
        }
        User userWithPhoneNumber =
                this.userRepository.findByPhoneNumber(user.getPhoneNumber());
        if (userWithPhoneNumber != null) {
            if (!userWithPhoneNumber.getId().equals(user.getId())) {
                throw new DuplicatePhoneNumberException("User", user.getPhoneNumber());
            }
        }
    }

    private boolean isAdmin(User loggedUser) {
        return loggedUser.getRole().equals(UserRole.ADMIN);
    }

    private boolean areSameUser(User loggedUser, User targetUser) {
        return targetUser.getUserName().equals(loggedUser.getUserName());
    }

    private void deleteUserFeedbacks(User targetUser) {
        for (Feedback feedback : targetUser.getFeedbacks()) {
            feedback.setDeleted(true);
        }
    }

    private void recoverUserFeedbacks(User targetUser) {
        for (Feedback feedback : targetUser.getFeedbacks()) {
            feedback.setDeleted(false);
        }
    }

    private void deleteUserTravels(User targetUser) {
        for (Travel travel : targetUser.getTravelsAsDriver()) {

            if (travel.getStatus().equals(TravelStatus.ACTIVE)) {
                throw new InvalidOperationException(ACTIVE_TRAVEL_EXCEPTION_MSG);
            }

            if (travel.getStatus().equals(TravelStatus.PLANNED)) {
                travel.setStatus(TravelStatus.CANCELED);
            }

            travel.setDeleted(true);
        }
    }


    private void recoverUserTravels(User targetUser) {
        for (Travel travel : targetUser.getTravelsAsDriver()) {
            travel.setDeleted(false);
        }

    }


}
