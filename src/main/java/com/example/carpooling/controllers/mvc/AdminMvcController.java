package com.example.carpooling.controllers.mvc;

import com.example.carpooling.exceptions.ActiveTravelException;
import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.models.Image;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.UserFilterDto;
import com.example.carpooling.models.dtos.UserPreviewDto;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.services.contracts.TravelService;
import com.example.carpooling.services.contracts.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.http.client.utils.URLEncodedUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminMvcController {
    private final UserService userService;
    private final AuthenticationHelper authenticationHelper;
    private final ModelMapper modelMapper;
    private final TravelService travelService;

    @Autowired
    public AdminMvcController(UserService userService, AuthenticationHelper authenticationHelper, ModelMapper modelMapper, TravelService travelService) {
        this.userService = userService;
        this.authenticationHelper = authenticationHelper;
        this.modelMapper = modelMapper;
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

    @GetMapping("/users")
    public String getUsers(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "5") int size,
                           @ModelAttribute("filter") UserFilterDto filter,
                           Model model,
                           HttpSession session,
                           HttpServletRequest request) {
        User loggedUser;
        try {

            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        if (!loggedUser.getRole().equals(UserRole.ADMIN)) {
            return "AccessDeniedView";
        }

        Sort sort;
        if (filter.getSortOrder().equalsIgnoreCase("desc")) {
            sort = Sort.by(Sort.Direction.DESC, filter.getSortBy());
        } else {
            sort = Sort.by(Sort.Direction.ASC, filter.getSortBy());
        }
        Page<User> users = userService.getItemsByPage(
                page,
                size,
                filter.getFirstName(),
                filter.getLastName(),
                filter.getUsername(),
                filter.getEmail(),
                filter.getPhoneNumber(),
                sort);


//        List<UserPreviewDto> users = this.userService.findAll(
//                        filter.getFirstName(),
//                        filter.getLastName(),
//                        filter.getUsername(),
//                        filter.getEmail(),
//                        filter.getPhoneNumber(),
//                        sort)
//                .stream()
//                .map(user -> {
//                    UserPreviewDto dto = this.modelMapper.map(user, UserPreviewDto.class);
//                    dto.setFullName(String.format("%s %s", user.getFirstName(), user.getLastName()));
//                    return dto;
//                })
//                .collect(Collectors.toList());

//        PageRequest pageRequest = PageRequest.of(page, size);
//        int start = (int) pageRequest.getOffset();
//        int end = Math.min((start + pageRequest.getPageSize()), users.size());
//
//        Page<UserPreviewDto> itemsByPage
//                = new PageImpl<UserPreviewDto>(users.subList(start, end), pageRequest, users.size());
        Map<String, String[]> parameterMap = request.getParameterMap();
        String parameters = extractParametersSection(parameterMap);
        model.addAttribute("filter", filter);
        model.addAttribute("userPage", users);
        model.addAttribute("filterParams", parameters);


//        model.addAttribute("users", users);
        return "UsersView";
    }

    private String extractParametersSection(Map<String, String[]> parameterMap) {
        StringBuilder builder = new StringBuilder();
        for (String key : parameterMap.keySet()) {
            String value = parameterMap.get(key)[0];
            if (value.trim().isEmpty() || key.equals("page")) {
                continue;
            }
            builder.append("&").append(key).append("=").append(value);
        }
        return builder.toString();
    }

    @GetMapping("/{id}/upgrade")
    public String upgradeUser(@PathVariable Long id, HttpSession session) {

        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        try {
            this.userService.upgrade(id, loggedUser);
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (AuthorizationException e) {
            return "AccessDeniedView";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/downgrade")
    public String downgradeUser(@PathVariable Long id, HttpSession session) {

        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        try {
            this.userService.downgrade(id, loggedUser);
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (AuthorizationException e) {
            return "AccessDeniedView";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/block")
    public String blockUser(@PathVariable Long id, Model model, HttpSession session) {

        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        try {
            this.userService.block(id, loggedUser);
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (AuthorizationException e) {
            return "AccessDeniedView";
        } catch (ActiveTravelException e) {

            model.addAttribute("user", this.userService.getById(id));
            return "ActiveTravelsOnBlockView";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/unblock")
    public String unblockUser(@PathVariable Long id, HttpSession session) {

        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        try {
            this.userService.unblock(id, loggedUser);
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (AuthorizationException e) {
            return "AccessDeniedView";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("{id}/complete-travels-block")
    public String completeActiveTravelsAndBlockUser(@PathVariable Long id, HttpSession session) {
        User loggedUser;
        try {
            loggedUser = authenticationHelper.tryGetUser(session);
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }

        try {
            User targetUser = this.userService.getById(id);
            travelService.completeActiveTravelsAndBlockUser(id, loggedUser);
            return "redirect:/auth/logout";
        } catch (EntityNotFoundException e) {
            return "NotFoundView";
        } catch (AuthorizationException e) {
            return "AccessDeniedView";
        }
    }
}
