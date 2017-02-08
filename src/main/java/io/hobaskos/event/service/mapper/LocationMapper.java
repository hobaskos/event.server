package io.hobaskos.event.service.mapper;

import io.hobaskos.event.domain.*;
import io.hobaskos.event.service.dto.LocationDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity Location and its DTO LocationDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface LocationMapper {

    @Mapping(source = "event.id", target = "eventId")
    LocationDTO locationToLocationDTO(Location location);

    List<LocationDTO> locationsToLocationDTOs(List<Location> locations);

    @Mapping(source = "eventId", target = "event")
    Location locationDTOToLocation(LocationDTO locationDTO);

    List<Location> locationDTOsToLocations(List<LocationDTO> locationDTOs);

    default Event eventFromId(Long id) {
        if (id == null) {
            return null;
        }
        Event event = new Event();
        event.setId(id);
        return event;
    }
}
