package io.hobaskos.event.service;

import io.hobaskos.event.service.dto.EventPollDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Service Interface for managing EventPoll.
 */
public interface EventPollService {

    /**
     * Save a eventPoll.
     *
     * @param eventPollDTO the entity to save
     * @return the persisted entity
     */
    EventPollDTO save(EventPollDTO eventPollDTO);

    /**
     *  Get all the eventPolls.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<EventPollDTO> findAll(Pageable pageable);

    /**
     *  Get the "id" eventPoll.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    EventPollDTO findOne(Long id);

    /**
     *  Delete the "id" eventPoll.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the eventPoll corresponding to the query.
     *
     *  @param query the query of the search
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<EventPollDTO> search(String query, Pageable pageable);
}
