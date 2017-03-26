package io.hobaskos.event.service;

import io.hobaskos.event.service.dto.EventImageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing EventImage.
 */
public interface EventImageService {

    /**
     * Save a eventImage.
     *
     * @param eventImageDTO the entity to save
     * @return the persisted entity
     */
    EventImageDTO save(EventImageDTO eventImageDTO);

    /**
     *  Get all the eventImages.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<EventImageDTO> findAll(Pageable pageable);

    /**
     * Get all event images for poll
     * @param id
     * @return
     */
    Optional<List<EventImageDTO>> findAllForEvent(Long id);

    /**
     *  Get the "id" eventImage.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    EventImageDTO findOne(Long id);

    /**
     *  Delete the "id" eventImage.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the eventImage corresponding to the query.
     *
     *  @param query the query of the search
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<EventImageDTO> search(String query, Pageable pageable);

    /**
     * Increase the voteCount for video
     * @param videoId
     */
    @Async
    void increaseVoteCount(Long videoId);

    /**
     * Decrease the voteCount for video
     * @param videoId
     */
    @Async
    void decreaseVoteCount(Long videoId);
}
