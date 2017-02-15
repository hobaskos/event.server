package io.hobaskos.event.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.hobaskos.event.domain.EventCategory;

import io.hobaskos.event.repository.EventCategoryRepository;
import io.hobaskos.event.repository.search.EventCategorySearchRepository;
import io.hobaskos.event.web.rest.util.HeaderUtil;
import io.hobaskos.event.web.rest.util.PaginationUtil;
import io.hobaskos.event.service.dto.EventCategoryDTO;
import io.hobaskos.event.service.mapper.EventCategoryMapper;

import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing EventCategory.
 */
@RestController
@RequestMapping("/api")
public class EventCategoryResource {

    private final Logger log = LoggerFactory.getLogger(EventCategoryResource.class);
        
    @Inject
    private EventCategoryRepository eventCategoryRepository;

    @Inject
    private EventCategoryMapper eventCategoryMapper;

    @Inject
    private EventCategorySearchRepository eventCategorySearchRepository;

    /**
     * POST  /event-categories : Create a new eventCategory.
     *
     * @param eventCategoryDTO the eventCategoryDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new eventCategoryDTO, or with status 400 (Bad Request) if the eventCategory has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/event-categories")
    @Timed
    public ResponseEntity<EventCategoryDTO> createEventCategory(@Valid @RequestBody EventCategoryDTO eventCategoryDTO) throws URISyntaxException {
        log.debug("REST request to save EventCategory : {}", eventCategoryDTO);
        if (eventCategoryDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("eventCategory", "idexists", "A new eventCategory cannot already have an ID")).body(null);
        }
        EventCategory eventCategory = eventCategoryMapper.eventCategoryDTOToEventCategory(eventCategoryDTO);
        eventCategory = eventCategoryRepository.save(eventCategory);
        EventCategoryDTO result = eventCategoryMapper.eventCategoryToEventCategoryDTO(eventCategory);
        eventCategorySearchRepository.save(eventCategory);
        return ResponseEntity.created(new URI("/api/event-categories/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("eventCategory", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /event-categories : Updates an existing eventCategory.
     *
     * @param eventCategoryDTO the eventCategoryDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated eventCategoryDTO,
     * or with status 400 (Bad Request) if the eventCategoryDTO is not valid,
     * or with status 500 (Internal Server Error) if the eventCategoryDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/event-categories")
    @Timed
    public ResponseEntity<EventCategoryDTO> updateEventCategory(@Valid @RequestBody EventCategoryDTO eventCategoryDTO) throws URISyntaxException {
        log.debug("REST request to update EventCategory : {}", eventCategoryDTO);
        if (eventCategoryDTO.getId() == null) {
            return createEventCategory(eventCategoryDTO);
        }
        EventCategory eventCategory = eventCategoryMapper.eventCategoryDTOToEventCategory(eventCategoryDTO);
        eventCategory = eventCategoryRepository.save(eventCategory);
        EventCategoryDTO result = eventCategoryMapper.eventCategoryToEventCategoryDTO(eventCategory);
        eventCategorySearchRepository.save(eventCategory);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("eventCategory", eventCategoryDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /event-categories : get all the eventCategories.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of eventCategories in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/event-categories")
    @Timed
    public ResponseEntity<List<EventCategoryDTO>> getAllEventCategories(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of EventCategories");
        Page<EventCategory> page = eventCategoryRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/event-categories");
        return new ResponseEntity<>(eventCategoryMapper.eventCategoriesToEventCategoryDTOs(page.getContent()), headers, HttpStatus.OK);
    }

    /**
     * GET  /event-categories/:id : get the "id" eventCategory.
     *
     * @param id the id of the eventCategoryDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the eventCategoryDTO, or with status 404 (Not Found)
     */
    @GetMapping("/event-categories/{id}")
    @Timed
    public ResponseEntity<EventCategoryDTO> getEventCategory(@PathVariable Long id) {
        log.debug("REST request to get EventCategory : {}", id);
        EventCategory eventCategory = eventCategoryRepository.findOne(id);
        EventCategoryDTO eventCategoryDTO = eventCategoryMapper.eventCategoryToEventCategoryDTO(eventCategory);
        return Optional.ofNullable(eventCategoryDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /event-categories/:id : delete the "id" eventCategory.
     *
     * @param id the id of the eventCategoryDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/event-categories/{id}")
    @Timed
    public ResponseEntity<Void> deleteEventCategory(@PathVariable Long id) {
        log.debug("REST request to delete EventCategory : {}", id);
        eventCategoryRepository.delete(id);
        eventCategorySearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("eventCategory", id.toString())).build();
    }

    /**
     * SEARCH  /_search/event-categories?query=:query : search for the eventCategory corresponding
     * to the query.
     *
     * @param query the query of the eventCategory search 
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/event-categories")
    @Timed
    public ResponseEntity<List<EventCategoryDTO>> searchEventCategories(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of EventCategories for query {}", query);
        Page<EventCategory> page = eventCategorySearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/event-categories");
        return new ResponseEntity<>(eventCategoryMapper.eventCategoriesToEventCategoryDTOs(page.getContent()), headers, HttpStatus.OK);
    }


}
