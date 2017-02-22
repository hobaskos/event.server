package io.hobaskos.event.service.mapper;

import io.hobaskos.event.domain.*;
import io.hobaskos.event.service.dto.UserConnectionDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity UserConnection and its DTO UserConnectionDTO.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, UserMapper.class, })
public interface UserConnectionMapper {

    @Mapping(source = "requester.id", target = "requesterId")
    @Mapping(source = "requestee.id", target = "requesteeId")
    UserConnectionDTO userConnectionToUserConnectionDTO(UserConnection userConnection);

    List<UserConnectionDTO> userConnectionsToUserConnectionDTOs(List<UserConnection> userConnections);

    @Mapping(source = "requesterId", target = "requester")
    @Mapping(source = "requesteeId", target = "requestee")
    UserConnection userConnectionDTOToUserConnection(UserConnectionDTO userConnectionDTO);

    List<UserConnection> userConnectionDTOsToUserConnections(List<UserConnectionDTO> userConnectionDTOs);
}
