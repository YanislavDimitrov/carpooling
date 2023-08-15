package com.example.carpooling.controllers.rest;

import com.example.carpooling.exceptions.AuthenticationFailureException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.helpers.AuthenticationHelper;
import com.example.carpooling.helpers.mappers.FeedbackMapper;
import com.example.carpooling.models.Feedback;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.FeedbackCreateDto;
import com.example.carpooling.models.dtos.FeedbackViewDto;
import com.example.carpooling.services.contracts.FeedbackService;
import com.example.carpooling.services.contracts.TravelService;
import com.example.carpooling.services.contracts.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/feedbacks")
public class FeedbackRestController {

    public static final String NOT_AUTHORIZED = "You are not authorized to access this endpoint!";
    private final FeedbackService feedbackService;
    private final TravelService travelService;
    private final UserService userService;

    private final FeedbackMapper feedbackMapper;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public FeedbackRestController(FeedbackService feedbackService, TravelService travelService, UserService userService, FeedbackMapper feedbackMapper, AuthenticationHelper authenticationHelper) {
        this.feedbackService = feedbackService;
        this.travelService = travelService;
        this.userService = userService;
        this.feedbackMapper = feedbackMapper;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping
    public List<FeedbackViewDto> get(@RequestHeader HttpHeaders headers,
                                     @RequestParam(required = false) Short rating,
                                     @RequestParam(required = false) String comment,
                                     @RequestParam(required = false, defaultValue = "id") String sortBy,
                                     @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        Sort sort;
        try {
            if (sortOrder.equalsIgnoreCase("desc")) {
                sort = Sort.by(Sort.Direction.DESC, sortBy);
            } else {
                sort = Sort.by(Sort.Direction.ASC, sortBy);
            }
            List<Feedback> filteredFeedbacks;
            if (rating != null || comment != null) {
                filteredFeedbacks = feedbackService.findByCriteria(rating, comment, sort);
            } else {
                filteredFeedbacks = feedbackService.findAll(sort);
            }
            User user = authenticationHelper.tryGetUser(headers);
            return filteredFeedbacks
                    .stream()
                    .map(feedbackMapper::toDtoFromFeedback)
                    .toList();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_AUTHORIZED);
        }
    }

    @GetMapping("/{id}")
    public FeedbackViewDto get(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        try {
            User user = authenticationHelper.tryGetUser(headers);
            return feedbackMapper.toDtoFromFeedback(feedbackService.getById(id));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthenticationFailureException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
//    @PostMapping("/travel/{travelId}/user/{userId}")
//    public FeedbackViewDto create(@PathVariable Long travelId,
//                                  @PathVariable Long userId,
//                                  @RequestHeader HttpHeaders headers,
//                                  @RequestBody FeedbackCreateDto feedbackCreateDto) {
//        try {
//            Travel travel = travelService.getById(travelId);
//            User creator = authenticationHelper.tryGetUser(headers);
//            User recipient = userService.getById(userId);
////            feedbackService.create();
//        }catch (EntityNotFoundException e) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
//        }catch (AuthenticationFailureException e) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,e.getMessage());
//        }
//    }
}
