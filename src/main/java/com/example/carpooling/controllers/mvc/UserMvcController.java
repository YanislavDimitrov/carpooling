package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.ActiveTravelException;
import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.models.Image;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.UserChangePasswordDto;
import com.example.carpooling.models.dtos.UserViewDto;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.repositories.contracts.ImageRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.ImageService;
import com.example.carpooling.services.contracts.TravelService;
import com.example.carpooling.services.contracts.UserService;
import jakarta.servlet.http.HttpSession;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    public UserMvcController(AuthenticationHelper authenticationHelper, UserService userService, UserRepository userRepository, ModelMapper modelMapper, ImageService imageService, ImageRepository imageRepository, TravelService travelService) {
        this.authenticationHelper = authenticationHelper;
        this.userService = userService;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
        this.travelService = travelService;
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
    public String getUserById(@PathVariable Long id, Model model, HttpSession session) {
        try {
            authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        try {
            User user = userService.getById(id);
            UserViewDto userNewViewDto = this.modelMapper.map(user, UserViewDto.class);
            model.addAttribute("user", userNewViewDto);
            model.addAttribute("userId", userNewViewDto.getId());
            return "UserView";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
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
            return "ActiveTravelsView";
        }
    }

    @PostMapping("{id}/avatar")
    public String uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
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

        if (!file.isEmpty()) {
            Image oldImage = targetUser.getProfilePicture();

            //Remove old picture from Cloudinary if such
            if (oldImage != null) {
                this.imageService.destroyImage(oldImage, targetUser.getUserName());
            }

            // Upload newImage to Cloudinary
            String imgUrl = this.imageService.uploadImage(file.getBytes(), targetUser.getUserName());

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
        return "ChangePasswordView";
    }
    @GetMapping("/complete-travels")
    public String completeTravelsAndDelete(HttpSession session) {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
        try {
           travelService.completeActiveTravels(loggedUser);
            return "redirect:/auth/logout";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (ActiveTravelException e) {
            return "ActiveTravelsView";
        }
    }


}
