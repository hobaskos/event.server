package io.hobaskos.event.service.mapper;

import io.hobaskos.event.domain.*;
import io.hobaskos.event.service.dto.EventImageVoteDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity EventImageVote and its DTO EventImageVoteDTO.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, })
public interface EventImageVoteMapper {

    @Mapping(source = "user.login", target = "userLogin")
    @Mapping(source = "eventImage.id", target = "eventImageId")
    EventImageVoteDTO eventImageVoteToEventImageVoteDTO(EventImageVote eventImageVote);

    List<EventImageVoteDTO> eventImageVotesToEventImageVoteDTOs(List<EventImageVote> eventImageVotes);

    @Mapping(target = "user", ignore = true)
    @Mapping(source = "eventImageId", target = "eventImage")
    EventImageVote eventImageVoteDTOToEventImageVote(EventImageVoteDTO eventImageVoteDTO);

    List<EventImageVote> eventImageVoteDTOsToEventImageVotes(List<EventImageVoteDTO> eventImageVoteDTOs);

    default EventImage eventImageFromId(Long id) {
        if (id == null) {
            return null;
        }
        EventImage eventImage = new EventImage();
        eventImage.setId(id);
        return eventImage;
    }
}
