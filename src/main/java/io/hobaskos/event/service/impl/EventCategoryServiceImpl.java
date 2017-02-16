package io.hobaskos.event.service.impl;

import io.hobaskos.event.service.EventCategoryService;
import io.hobaskos.event.domain.EventCategory;
import io.hobaskos.event.repository.EventCategoryRepository;
import io.hobaskos.event.repository.search.EventCategorySearchRepository;
import io.hobaskos.event.service.StorageService;
import io.hobaskos.event.service.dto.EventCategoryDTO;
import io.hobaskos.event.service.mapper.EventCategoryMapper;
import io.hobaskos.event.service.util.ContentTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing EventCategory.
 */
@Service
@Transactional
public class EventCategoryServiceImpl implements EventCategoryService{

    private final Logger log = LoggerFactory.getLogger(EventCategoryServiceImpl.class);

    @Inject
    private EventCategoryRepository eventCategoryRepository;

    @Inject
    private EventCategoryMapper eventCategoryMapper;

    @Inject
    private EventCategorySearchRepository eventCategorySearchRepository;

    @Inject
    private StorageService storageService;

    /**
     * Save a eventCategory.
     *
     * @param eventCategoryDTO the entity to save
     * @return the persisted entity
     */
    public EventCategoryDTO save(EventCategoryDTO eventCategoryDTO) {
        log.debug("Request to save EventCategory : {}", eventCategoryDTO);
        EventCategory eventCategory = eventCategoryMapper.eventCategoryDTOToEventCategory(eventCategoryDTO);

        if (eventCategoryDTO.getIcon() != null && eventCategoryDTO.getIconContentType() != null) {
            String filename = storageService.store(eventCategoryDTO.getIcon(),
                ContentTypeUtil.defineImageName(eventCategoryDTO.getIconContentType()));
            eventCategory.setIconUrl("/files/" + filename);
        }

        eventCategory = eventCategoryRepository.save(eventCategory);
        EventCategoryDTO result = eventCategoryMapper.eventCategoryToEventCategoryDTO(eventCategory);
        eventCategorySearchRepository.save(eventCategory);
        return result;
    }

    /**
     *  Get all the eventCategories.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EventCategoryDTO> findAll(Pageable pageable) {
        log.debug("Request to get all EventCategories");
        Page<EventCategory> result = eventCategoryRepository.findAll(pageable);
        return result.map(eventCategory -> eventCategoryMapper.eventCategoryToEventCategoryDTO(eventCategory));
    }

    /**
     *  Get one eventCategory by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public EventCategoryDTO findOne(Long id) {
        log.debug("Request to get EventCategory : {}", id);
        EventCategory eventCategory = eventCategoryRepository.findOne(id);
        EventCategoryDTO eventCategoryDTO = eventCategoryMapper.eventCategoryToEventCategoryDTO(eventCategory);
        return eventCategoryDTO;
    }

    /**
     *  Delete the  eventCategory by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete EventCategory : {}", id);
        eventCategoryRepository.delete(id);
        eventCategorySearchRepository.delete(id);
    }

    /**
     * Search for the eventCategory corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EventCategoryDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of EventCategories for query {}", query);
        Page<EventCategory> result = eventCategorySearchRepository.search(queryStringQuery(query), pageable);
        return result.map(eventCategory -> eventCategoryMapper.eventCategoryToEventCategoryDTO(eventCategory));
    }
}
