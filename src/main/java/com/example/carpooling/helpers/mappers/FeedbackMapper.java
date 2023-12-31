package com.example.carpooling.helpers.mappers;

import com.example.carpooling.models.Feedback;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.dtos.FeedbackCreateDto;
import com.example.carpooling.models.dtos.FeedbackViewDto;
import com.example.carpooling.services.contracts.FeedbackService;
import com.example.carpooling.services.contracts.TravelService;
import com.example.carpooling.services.contracts.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {

    private final FeedbackService feedbackService;
    private final TravelService travelService;
    private final UserService userService;

    @Autowired
    public FeedbackMapper(FeedbackService feedbackService, TravelService travelService, UserService userService) {
        this.feedbackService = feedbackService;
        this.travelService = travelService;
        this.userService = userService;
    }

    public FeedbackViewDto toDtoFromFeedback(Feedback feedback) {
        FeedbackViewDto feedbackViewDto = new FeedbackViewDto();
        feedbackViewDto.setTravelId(feedback.getTravel().getId());
        feedbackViewDto.setRecipient(feedback.getRecipient().getUserName());
        feedbackViewDto.setCreator(feedback.getCreator().getUserName());
        feedbackViewDto.setRating(feedback.getRating());
        feedbackViewDto.setComment(feedback.getComment());
        return feedbackViewDto;
    }

    public Feedback toFeedbackFromFeedbackViewDto(FeedbackViewDto feedbackViewDto, Long id) {
        Feedback feedback = feedbackService.getById(id);
        feedback.setTravel(travelService.getById(feedbackViewDto.getTravelId()));
        feedback.setCreator(userService.getByUsername(feedbackViewDto.getCreator()));
        feedback.setRecipient(userService.getByUsername(feedbackViewDto.getRecipient()));
        feedback.setRating(feedbackViewDto.getRating());
        feedback.setComment(feedbackViewDto.getComment());
        return feedback;
    }

    public Feedback fromCreationDto(FeedbackCreateDto feedbackCreateDto, User creator, User recipient, Travel travel) {
        Feedback feedback = new Feedback();
        feedback.setComment(feedbackCreateDto.getComment());
        feedback.setRating(feedbackCreateDto.getRating());
        feedback.setCreator(creator);
        feedback.setRecipient(recipient);
        feedback.setTravel(travel);

        return feedback;
    }

    public FeedbackCreateDto fromFeedback(Feedback feedback) {
        FeedbackCreateDto feedbackCreateDto = new FeedbackCreateDto();
        feedbackCreateDto.setComment(feedback.getComment());
        feedbackCreateDto.setRating(feedback.getRating());
        return feedbackCreateDto;
    }

    public Feedback fromUpdateToFeedback(FeedbackCreateDto feedbackCreateDto, Long id) {
        Feedback feedback = feedbackService.getById(id);
        feedback.setComment(feedbackCreateDto.getComment());
        feedback.setRating(feedbackCreateDto.getRating());
        return feedback;
    }
}
