package io.hobaskos.event.service.mapper;

import io.hobaskos.event.domain.*;
import io.hobaskos.event.repository.EventImageVoteRepository;
import io.hobaskos.event.repository.EventPollRepository;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.dto.EventImageDTO;

import org.mapstruct.*;

import javax.inject.Inject;
import java.util.List;

/**
 * Mapper for the entity EventImage and its DTO EventImageDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public abstract class EventImageMapper {

    @Inject
    private UserService userService;

    @Inject
    private EventImageVoteRepository eventImageVoteRepository;

    @Mapping(target = "file", ignore = true)
    @Mapping(target = "fileContentType", ignore = true)
    @Mapping(source = "poll.id", target = "pollId")
    @Mapping(source = "user.login", target = "userLogin")
    public abstract EventImageDTO eventImageToEventImageDTO(EventImage eventImage);

    public abstract List<EventImageDTO> eventImagesToEventImageDTOs(List<EventImage> eventImages);

    @Mapping(source = "pollId", target = "poll")
    @Mapping(target = "votes", ignore = true)
    @Mapping(target = "user", ignore = true)
    public abstract EventImage eventImageDTOToEventImage(EventImageDTO eventImageDTO);

    public abstract List<EventImage> eventImageDTOsToEventImages(List<EventImageDTO> eventImageDTOs);

    public EventPoll eventPollFromId(Long id) {
        if (id == null) {
            return null;
        }
        EventPoll eventPoll = new EventPoll();
        eventPoll.setId(id);
        return eventPoll;
    }

    @AfterMapping
    protected void sideLoadMetaData(EventImage eventImage, @MappingTarget EventImageDTO result) {
        User user = userService.getUserWithAuthorities();
        if (user == null) return;
        eventImageVoteRepository.findFirstByEventImageAndUser(eventImage, user)
            .ifPresent(eventImageVote -> result.setHasMyVote(true));
    }
}
