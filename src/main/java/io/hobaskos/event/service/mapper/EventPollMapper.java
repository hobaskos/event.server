package io.hobaskos.event.service.mapper;

import io.hobaskos.event.domain.*;
import io.hobaskos.event.service.dto.EventPollDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity EventPoll and its DTO EventPollDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface EventPollMapper {

    @Mapping(source = "event.id", target = "eventId")
    EventPollDTO eventPollToEventPollDTO(EventPoll eventPoll);

    List<EventPollDTO> eventPollsToEventPollDTOs(List<EventPoll> eventPolls);

    @Mapping(source = "eventId", target = "event")
    EventPoll eventPollDTOToEventPoll(EventPollDTO eventPollDTO);

    List<EventPoll> eventPollDTOsToEventPolls(List<EventPollDTO> eventPollDTOs);

    default Event eventFromId(Long id) {
        if (id == null) {
            return null;
        }
        Event event = new Event();
        event.setId(id);
        return event;
    }
}
