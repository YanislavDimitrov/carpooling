package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.models.Image;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.UserViewDto;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.repositories.contracts.ImageRepository;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.services.contracts.ImageService;
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

    public UserMvcController(AuthenticationHelper authenticationHelper, UserService userService, UserRepository userRepository, ModelMapper modelMapper, ImageService imageService, ImageRepository imageRepository) {
        this.authenticationHelper = authenticationHelper;
        this.userService = userService;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
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
            return "UserView";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "NotFoundView";
            //ToDO
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
            // Upload image to Cloudinary
            String imgUrl = this.imageService.uploadImage(file.getBytes());

            // Save image URL to database
            Image image = new Image();
            image.setImageUrl(imgUrl);
            this.imageRepository.save(image);
            targetUser.setProfilePicture(image);
            this.userRepository.save(targetUser);
        }
        return "redirect:/";
    }
}
