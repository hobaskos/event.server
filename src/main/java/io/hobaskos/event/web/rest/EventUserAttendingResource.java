package io.hobaskos.event.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.hobaskos.event.domain.EventUserAttending;

import io.hobaskos.event.repository.EventUserAttendingRepository;
import io.hobaskos.event.repository.search.EventUserAttendingSearchRepository;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.web.rest.util.HeaderUtil;
import io.hobaskos.event.web.rest.util.PaginationUtil;
import io.hobaskos.event.service.dto.EventUserAttendingDTO;
import io.hobaskos.event.service.mapper.EventUserAttendingMapper;

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
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing EventUserAttending.
 */
@RestController
@RequestMapping("/api")
public class EventUserAttendingResource {

    private final Logger log = LoggerFactory.getLogger(EventUserAttendingResource.class);

    @Inject
    private EventUserAttendingRepository eventUserAttendingRepository;

    @Inject
    private EventUserAttendingMapper eventUserAttendingMapper;

    @Inject
    private EventUserAttendingSearchRepository eventUserAttendingSearchRepository;

    @Inject
    private UserService userService;

    /**
     * POST  /event-user-attendings : Create a new eventUserAttending.
     *
     * @param eventUserAttendingDTO the eventUserAttendingDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new eventUserAttendingDTO, or with status 400 (Bad Request) if the eventUserAttending has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/event-user-attendings")
    @Timed
    public ResponseEntity<EventUserAttendingDTO> createEventUserAttending(@Valid @RequestBody EventUserAttendingDTO eventUserAttendingDTO) throws URISyntaxException {
        log.debug("REST request to save EventUserAttending : {}", eventUserAttendingDTO);
        if (eventUserAttendingDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("eventUserAttending", "idexists", "A new eventUserAttending cannot already have an ID")).body(null);
        }
        EventUserAttending eventUserAttending = eventUserAttendingMapper.eventUserAttendingDTOToEventUserAttending(eventUserAttendingDTO);
        eventUserAttending.setCreatedDate(ZonedDateTime.now());
        eventUserAttending.setUser(userService.getUserWithAuthorities());
        eventUserAttending = eventUserAttendingRepository.save(eventUserAttending);
        EventUserAttendingDTO result = eventUserAttendingMapper.eventUserAttendingToEventUserAttendingDTO(eventUserAttending);
        eventUserAttendingSearchRepository.save(eventUserAttending);
        return ResponseEntity.created(new URI("/api/event-user-attendings/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("eventUserAttending", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /event-user-attendings : Updates an existing eventUserAttending.
     *
     * @param eventUserAttendingDTO the eventUserAttendingDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated eventUserAttendingDTO,
     * or with status 400 (Bad Request) if the eventUserAttendingDTO is not valid,
     * or with status 500 (Internal Server Error) if the eventUserAttendingDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/event-user-attendings")
    @Timed
    public ResponseEntity<EventUserAttendingDTO> updateEventUserAttending(@Valid @RequestBody EventUserAttendingDTO eventUserAttendingDTO) throws URISyntaxException {
        log.debug("REST request to update EventUserAttending : {}", eventUserAttendingDTO);
        if (eventUserAttendingDTO.getId() == null) {
            return createEventUserAttending(eventUserAttendingDTO);
        }
        EventUserAttending eventUserAttending = eventUserAttendingMapper.eventUserAttendingDTOToEventUserAttending(eventUserAttendingDTO);
        eventUserAttending = eventUserAttendingRepository.save(eventUserAttending);
        EventUserAttendingDTO result = eventUserAttendingMapper.eventUserAttendingToEventUserAttendingDTO(eventUserAttending);
        eventUserAttendingSearchRepository.save(eventUserAttending);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("eventUserAttending", eventUserAttendingDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /event-user-attendings : get all the eventUserAttendings.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of eventUserAttendings in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/event-user-attendings")
    @Timed
    public ResponseEntity<List<EventUserAttendingDTO>> getAllEventUserAttendings(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of EventUserAttendings");
        Page<EventUserAttending> page = eventUserAttendingRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/event-user-attendings");
        return new ResponseEntity<>(eventUserAttendingMapper.eventUserAttendingsToEventUserAttendingDTOs(page.getContent()), headers, HttpStatus.OK);
    }

    /**
     * GET  /event-user-attendings/:id : get the "id" eventUserAttending.
     *
     * @param id the id of the eventUserAttendingDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the eventUserAttendingDTO, or with status 404 (Not Found)
     */
    @GetMapping("/event-user-attendings/{id}")
    @Timed
    public ResponseEntity<EventUserAttendingDTO> getEventUserAttending(@PathVariable Long id) {
        log.debug("REST request to get EventUserAttending : {}", id);
        EventUserAttending eventUserAttending = eventUserAttendingRepository.findOne(id);
        EventUserAttendingDTO eventUserAttendingDTO = eventUserAttendingMapper.eventUserAttendingToEventUserAttendingDTO(eventUserAttending);
        return Optional.ofNullable(eventUserAttendingDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /event-user-attendings/:id : delete the "id" eventUserAttending.
     *
     * @param id the id of the eventUserAttendingDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/event-user-attendings/{id}")
    @Timed
    public ResponseEntity<Void> deleteEventUserAttending(@PathVariable Long id) {
        log.debug("REST request to delete EventUserAttending : {}", id);
        eventUserAttendingRepository.delete(id);
        eventUserAttendingSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("eventUserAttending", id.toString())).build();
    }

    /**
     * SEARCH  /_search/event-user-attendings?query=:query : search for the eventUserAttending corresponding
     * to the query.
     *
     * @param query the query of the eventUserAttending search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/event-user-attendings")
    @Timed
    public ResponseEntity<List<EventUserAttendingDTO>> searchEventUserAttendings(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of EventUserAttendings for query {}", query);
        Page<EventUserAttending> page = eventUserAttendingSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/event-user-attendings");
        return new ResponseEntity<>(eventUserAttendingMapper.eventUserAttendingsToEventUserAttendingDTOs(page.getContent()), headers, HttpStatus.OK);
    }


}
