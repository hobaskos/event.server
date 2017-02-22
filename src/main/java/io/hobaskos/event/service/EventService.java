package io.hobaskos.event.service;

import io.hobaskos.event.domain.EventCategory;
import io.hobaskos.event.service.dto.EventDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service Interface for managing Event.
 */
public interface EventService {

    /**
     * Save a event.
     *
     * @param eventDTO the entity to save
     * @return the persisted entity
     */
    EventDTO save(EventDTO eventDTO);

    /**
     *  Get all the events.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<EventDTO> findAll(Pageable pageable);

    /**
     * Get all events with event category
     * @param eventCategory
     * @param pageable
     * @return the list of entities
     */
    Page<EventDTO> findAllWithEventCategory(EventCategory eventCategory, Pageable pageable);

    /**
     *  Get the "id" event.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    EventDTO findOne(Long id);

    /**
     *  Delete the "id" event.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the event corresponding to the query.
     *
     *  @param query the query of the search
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<EventDTO> search(String query, Pageable pageable);


    /**
     * Search for events nearby
     * @param lat
     * @param lon
     * @param distance
     * @return the list of entities
     */
    Page<EventDTO> searchNearby(String query, Double lat, Double lon, String distance, LocalDateTime fromDate,
                                LocalDateTime toDate, List<Long> eventCategoryIds, Pageable pageable);
}
