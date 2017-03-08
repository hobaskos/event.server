package io.hobaskos.event.service.mapper;

import io.hobaskos.event.domain.*;
import io.hobaskos.event.service.dto.EventUserAttendingDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity EventUserAttending and its DTO EventUserAttendingDTO.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, })
public interface EventUserAttendingMapper {

    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "user.login", target = "userLogin")
    EventUserAttendingDTO eventUserAttendingToEventUserAttendingDTO(EventUserAttending eventUserAttending);

    List<EventUserAttendingDTO> eventUserAttendingsToEventUserAttendingDTOs(List<EventUserAttending> eventUserAttendings);

    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "userLogin", target = "user")
    EventUserAttending eventUserAttendingDTOToEventUserAttending(EventUserAttendingDTO eventUserAttendingDTO);

    List<EventUserAttending> eventUserAttendingDTOsToEventUserAttendings(List<EventUserAttendingDTO> eventUserAttendingDTOs);

    default Event eventFromId(Long id) {
        if (id == null) {
            return null;
        }
        Event event = new Event();
        event.setId(id);
        return event;
    }
}
