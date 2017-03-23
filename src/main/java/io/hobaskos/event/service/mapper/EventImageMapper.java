package io.hobaskos.event.service.mapper;

import io.hobaskos.event.domain.*;
import io.hobaskos.event.service.dto.EventImageDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity EventImage and its DTO EventImageDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface EventImageMapper {

    @Mapping(target = "file", ignore = true)
    @Mapping(target = "fileContentType", ignore = true)
    @Mapping(source = "poll.id", target = "pollId")
    @Mapping(source = "user.login", target = "userLogin")
    EventImageDTO eventImageToEventImageDTO(EventImage eventImage);

    List<EventImageDTO> eventImagesToEventImageDTOs(List<EventImage> eventImages);

    @Mapping(source = "pollId", target = "poll")
    @Mapping(target = "votes", ignore = true)
    @Mapping(target = "user", ignore = true)
    EventImage eventImageDTOToEventImage(EventImageDTO eventImageDTO);

    List<EventImage> eventImageDTOsToEventImages(List<EventImageDTO> eventImageDTOs);

    default EventPoll eventPollFromId(Long id) {
        if (id == null) {
            return null;
        }
        EventPoll eventPoll = new EventPoll();
        eventPoll.setId(id);
        return eventPoll;
    }
}
