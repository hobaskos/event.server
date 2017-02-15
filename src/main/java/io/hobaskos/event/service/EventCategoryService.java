package io.hobaskos.event.service;

import io.hobaskos.event.service.dto.EventCategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Service Interface for managing EventCategory.
 */
public interface EventCategoryService {

    /**
     * Save a eventCategory.
     *
     * @param eventCategoryDTO the entity to save
     * @return the persisted entity
     */
    EventCategoryDTO save(EventCategoryDTO eventCategoryDTO);

    /**
     *  Get all the eventCategories.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<EventCategoryDTO> findAll(Pageable pageable);

    /**
     *  Get the "id" eventCategory.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    EventCategoryDTO findOne(Long id);

    /**
     *  Delete the "id" eventCategory.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the eventCategory corresponding to the query.
     *
     *  @param query the query of the search
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<EventCategoryDTO> search(String query, Pageable pageable);
}
