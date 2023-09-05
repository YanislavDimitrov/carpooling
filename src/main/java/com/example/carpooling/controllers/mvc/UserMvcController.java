package com.example.carpooling.controllers.mvc;
import com.example.carpooling.exceptions.ActiveTravelException;
import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.models.Image;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.UserChangePasswordDto;
import com.example.carpooling.models.dtos.UserUpdateDto;
import com.example.carpooling.models.dtos.UserViewDto;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.models.enums.UserStatus;
import com.example.carpooling.repositories.contracts.ImageRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.ImageService;
import com.example.carpooling.services.contracts.TravelService;
import com.example.carpooling.services.contracts.UserService;
import com.example.carpooling.services.contracts.ValidationService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@Controller
@RequestMapping("/users")
public class UserMvcController {
    private final AuthenticationHelper authenticationHelper;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final TravelService travelService;
    private final ValidationService validationService;

    public UserMvcController(AuthenticationHelper authenticationHelper, UserService userService, UserRepository userRepository, ModelMapper modelMapper, ImageService imageService, ImageRepository imageRepository, TravelService travelService, ValidationService validationService) {
        this.authenticationHelper = authenticationHelper;
        this.userService = userService;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
        this.travelService = travelService;
        this.validationService = validationService;
    }

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

    @GetMapping("/{id}")
    public String getUserById(@PathVariable Long id, Model model) {

        try {
            User user = userService.getById(id);
            if (user.getStatus().equals(UserStatus.DELETED)) {
                return "NotFoundView";
            }
            UserViewDto userNewViewDto = this.modelMapper.map(user, UserViewDto.class);
            model.addAttribute("user", userNewViewDto);
            model.addAttribute("vehiclesCount", user.getVehiclesCount());
            return "UserView";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, Model model, HttpSession session) {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        try {
            this.userService.delete(id, loggedUser);
            return "redirect:/auth/logout";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (ActiveTravelException e) {
            model.addAttribute("userId", id);
            return "ActiveTravelsOnDeleteView";
        }
    }

    @GetMapping("/{id}/update")
    public String getUpdateUser(@PathVariable Long id, Model model, HttpSession session) {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        try {
            User targetUser = this.userService.getById(id);
            if (!loggedUser.getUserName().equals(targetUser.getUserName())) {
                return "AccessDeniedView";
            }
            UserUpdateDto updateDto = this.modelMapper.map(targetUser, UserUpdateDto.class);
            model.addAttribute("user", updateDto);
            return "UpdateUserView";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }
    }

    @PostMapping("/{id}/update")
    public String updateUser(@Valid @ModelAttribute("user") UserUpdateDto dto,
                             BindingResult bindingResult,
                             @PathVariable Long id,
                             HttpSession session) throws MessagingException, IOException {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            return "UpdateUserView";
        }

        User targetUser = this.userService.getById(id);

        boolean revalidationRequired = !targetUser.getEmail().equals(dto.getEmail());
        userService.update(id, dto, loggedUser);

        if (revalidationRequired) {
            userService.unverify(id);
            this.validationService.validate(targetUser);
            return "VerificationLinkSentView";
        }
        return String.format("redirect:/users/%d", id);
    }

    @PostMapping("{id}/avatar")
    public String uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        User targetUser;
        try {
            targetUser = this.userService.getById(id);
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }

        if (!loggedUser.getRole().equals(UserRole.ADMIN) &&
                !loggedUser.getUserName().equals(targetUser.getUserName())) {
            return "AccessDeniedView";
        }

        if (!file.isEmpty()) {
            Image oldImage = targetUser.getProfilePicture();

            //Remove old picture from Cloudinary if such
            if (oldImage != null) {
                this.imageService.destroyImage(oldImage, targetUser.getUserName());
            }

            // Upload newImage to Cloudinary
            String imgUrl = (String) this.imageService.uploadImage(file.getBytes(), targetUser.getUserName())
                    .get("secure_url");

            // Save newImage URL to database
            Image newImage = new Image();
            newImage.setImageUrl(imgUrl);
            this.imageRepository.save(newImage);

            // Update User profile picture
            targetUser.setProfilePicture(newImage);
            this.userRepository.save(targetUser);

            //Remove old picture from DB
            if (oldImage != null) {
                this.imageService.delete(oldImage);
            }
        }
        return String.format("redirect:/users/%d", id);
    }

    @GetMapping("{id}/change-password")
    public String getChangePassword(@PathVariable Long id, Model model, HttpSession session) {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        User targetUser = this.userService.getById(id);

        if (!loggedUser.getRole().equals(UserRole.ADMIN) &&
                !loggedUser.getUserName().equals(targetUser.getUserName())) {
            return "AccessDeniedView";
        }
        model.addAttribute("changePasswordInfo", new UserChangePasswordDto());
        model.addAttribute("userId", id);
        return "ChangePasswordView";
    }

    @PostMapping("{id}/change-password")
    public String changePassword(@Valid @ModelAttribute("changePasswordInfo") UserChangePasswordDto dto,
                                 BindingResult bindingResult,
                                 Model model,
                                 @PathVariable Long id, HttpSession session) {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
        User targetUser;
        try {
            targetUser = this.userService.getById(id);
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }

        if (!dto.getOldPassword().equals(targetUser.getPassword())) {
            bindingResult.rejectValue("oldPassword",
                    "password_error",
                    "Wrong password");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            bindingResult.rejectValue("confirmNewPassword",
                    "password_error",
                    "New password and Confirm New password don't match");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("userId", id);
            return "ChangePasswordView";
        }

        try {
            this.userService.changePassword(targetUser, dto, loggedUser);
        } catch (AuthorizationException e) {
            return "AccessDeniedView";
        }
        return "PasswordChangeSuccessView";
    }

    @GetMapping("{id}/complete-travels-delete")
    public String completeActiveTravelsAndDeleteUser(@PathVariable Long id, HttpSession session) {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        User targetUser = this.userService.getById(id);

        if (!loggedUser.getUserName().equals(targetUser.getUserName())) {
            return "AccessDeniedView";
        }

        try {
            travelService.completeActiveTravelsAndDeleteUser(loggedUser);
            return "redirect:/auth/logout";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }
    }

    @GetMapping("{id}/validate")
    public String verifyUser(@PathVariable Long id, HttpSession session) throws MessagingException, IOException {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        if (!loggedUser.getRole().equals(UserRole.ADMIN) && (!loggedUser.getId().equals(id))) {
            return "AccessDeniedView";
        }

        try {
            this.validationService.validate(this.userService.getById(id));
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }
        return "VerificationLinkSentView";
    }
}
