package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.exceptions.duplicate.DuplicateEmailException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.exceptions.duplicate.DuplicatePhoneNumberException;
import com.example.carpooling.exceptions.duplicate.DuplicateUsernameException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.models.Image;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.LoginDto;
import com.example.carpooling.models.dtos.RegisterDto;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.models.enums.UserStatus;
import com.example.carpooling.services.contracts.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/auth")
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationHelper authenticationHelper;
    private final ModelMapper modelMapper;

    @Autowired
    public AuthenticationController(UserService userService, AuthenticationHelper authenticationHelper, ModelMapper modelMapper) {
        this.userService = userService;
        this.authenticationHelper = authenticationHelper;
        this.modelMapper = modelMapper;
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
    @ModelAttribute("isBlocked")
    public boolean populateIsActive(HttpSession session) {
        try {
            User loggedUser = authenticationHelper.tryGetUser(session);
            return loggedUser.getStatus() == UserStatus.BLOCKED;
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

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("login", new LoginDto());
        return "LoginView";
        //Todo LoginView
    }
    @GetMapping("/recovered")
    public String recoverAccountPage() {
        return "DeletedUserView";
    }

    @PostMapping("/login")
    public String handleLogin(@Valid @ModelAttribute("login") LoginDto dto,
                              BindingResult bindingResult,
                              HttpSession session) {

        if (bindingResult.hasErrors()) {
            return "LoginView";
        }

        try {
            User user = userService.getByUsername(dto.getUserName());
            authenticationHelper.verifyAuthentication(user.getUserName(), dto.getPassword());

            if (user.getStatus() == UserStatus.DELETED) {
                userService.restore(user.getId(), user);
                session.setAttribute("currentUser", dto.getUserName());
                return "redirect:/auth/recovered";
            }

            session.setAttribute("currentUser", dto.getUserName());
            session.setAttribute("id", user.getId());

            return "redirect:/";

        } catch (EntityNotFoundException | AuthenticationFailureException e) {
            bindingResult.rejectValue("userName", "auth_error", "Username/password not correct");
            return "LoginView";
        }
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.removeAttribute("currentUser");
        return "redirect:/";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("register", new RegisterDto());
        return "RegisterView";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("register") RegisterDto registerDto,
                                 BindingResult bindingResult) throws MessagingException {
        if (bindingResult.hasErrors()) {
            return "RegisterView";
        }
        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword",
                    "password_error",
                    "Password and Confirm password don't match");
            return "RegisterView";
        }
        try {
            User user = this.modelMapper.map(registerDto, User.class);
            this.userService.create(user);
            return "VerificationLinkSentView";
        } catch (DuplicateUsernameException e) {
            bindingResult.rejectValue("userName",
                    "username_error",
                    e.getMessage());
            return "RegisterView";
        } catch (DuplicateEmailException e) {
            bindingResult.rejectValue("email", "email_error", e.getMessage());
            return "RegisterView";
        } catch (DuplicatePhoneNumberException e) {
            bindingResult.rejectValue("phoneNumber", "phoneNumber_error,", e.getMessage());
            return "RegisterView";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
