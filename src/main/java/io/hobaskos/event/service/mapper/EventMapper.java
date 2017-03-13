package io.hobaskos.event.service.mapper;

import io.hobaskos.event.domain.*;
import io.hobaskos.event.domain.enumeration.EventAttendingType;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.dto.EventDTO;

import org.mapstruct.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Mapper for the entity Event and its DTO EventDTO.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, LocationMapper.class, EventCategoryMapper.class})
public abstract class EventMapper {

    @Inject
    private UserService userService;

    @Mapping(target = "image", ignore = true)
    @Mapping(target = "imageContentType", ignore = true)
    @Mapping(source = "owner.login", target = "ownerLogin")
    @Mapping(source = "attendings", target = "myAttendance")
    @Mapping(source = "attendings", target = "attendanceCount")
    public abstract EventDTO eventToEventDTO(Event event);

    public abstract List<EventDTO> eventsToEventDTOs(List<Event> events);

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "attendings", ignore = true)
    @Mapping(target = "polls", ignore = true)
    public abstract Event eventDTOToEvent(EventDTO eventDTO);

    public abstract List<Event> eventDTOsToEvents(List<EventDTO> eventDTOs);

    public EventAttendingType attendingsToMyAttendance(Set<EventUserAttending> attendings) {
        if (attendings == null) return null;
        User user = userService.getUserWithAuthorities();
        return attendings.stream().filter(eventUserAttending ->
            eventUserAttending.getUser().equals(user)).findFirst()
            .map(EventUserAttending::getType)
            .orElse(null);
    }

    public int attendingsToAttendanceCount(Set<EventUserAttending> attendings) {
        if (attendings == null) return 0;
        return attendings.size();
    }
}
