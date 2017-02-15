package io.hobaskos.event.service.mapper;

import io.hobaskos.event.domain.*;
import io.hobaskos.event.service.dto.EventCategoryDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity EventCategory and its DTO EventCategoryDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface EventCategoryMapper {

    EventCategoryDTO eventCategoryToEventCategoryDTO(EventCategory eventCategory);

    List<EventCategoryDTO> eventCategoriesToEventCategoryDTOs(List<EventCategory> eventCategories);

    @Mapping(target = "events", ignore = true)
    EventCategory eventCategoryDTOToEventCategory(EventCategoryDTO eventCategoryDTO);

    List<EventCategory> eventCategoryDTOsToEventCategories(List<EventCategoryDTO> eventCategoryDTOs);
}
