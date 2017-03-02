package io.hobaskos.event.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.hobaskos.event.service.EventPollService;
import io.hobaskos.event.web.rest.util.HeaderUtil;
import io.hobaskos.event.web.rest.util.PaginationUtil;
import io.hobaskos.event.service.dto.EventPollDTO;

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
 * REST controller for managing EventPoll.
 */
@RestController
@RequestMapping("/api")
public class EventPollResource {

    private final Logger log = LoggerFactory.getLogger(EventPollResource.class);
        
    @Inject
    private EventPollService eventPollService;

    /**
     * POST  /event-polls : Create a new eventPoll.
     *
     * @param eventPollDTO the eventPollDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new eventPollDTO, or with status 400 (Bad Request) if the eventPoll has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/event-polls")
    @Timed
    public ResponseEntity<EventPollDTO> createEventPoll(@Valid @RequestBody EventPollDTO eventPollDTO) throws URISyntaxException {
        log.debug("REST request to save EventPoll : {}", eventPollDTO);
        if (eventPollDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("eventPoll", "idexists", "A new eventPoll cannot already have an ID")).body(null);
        }
        EventPollDTO result = eventPollService.save(eventPollDTO);
        return ResponseEntity.created(new URI("/api/event-polls/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("eventPoll", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /event-polls : Updates an existing eventPoll.
     *
     * @param eventPollDTO the eventPollDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated eventPollDTO,
     * or with status 400 (Bad Request) if the eventPollDTO is not valid,
     * or with status 500 (Internal Server Error) if the eventPollDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/event-polls")
    @Timed
    public ResponseEntity<EventPollDTO> updateEventPoll(@Valid @RequestBody EventPollDTO eventPollDTO) throws URISyntaxException {
        log.debug("REST request to update EventPoll : {}", eventPollDTO);
        if (eventPollDTO.getId() == null) {
            return createEventPoll(eventPollDTO);
        }
        EventPollDTO result = eventPollService.save(eventPollDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("eventPoll", eventPollDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /event-polls : get all the eventPolls.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of eventPolls in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/event-polls")
    @Timed
    public ResponseEntity<List<EventPollDTO>> getAllEventPolls(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of EventPolls");
        Page<EventPollDTO> page = eventPollService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/event-polls");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /event-polls/:id : get the "id" eventPoll.
     *
     * @param id the id of the eventPollDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the eventPollDTO, or with status 404 (Not Found)
     */
    @GetMapping("/event-polls/{id}")
    @Timed
    public ResponseEntity<EventPollDTO> getEventPoll(@PathVariable Long id) {
        log.debug("REST request to get EventPoll : {}", id);
        EventPollDTO eventPollDTO = eventPollService.findOne(id);
        return Optional.ofNullable(eventPollDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /event-polls/:id : delete the "id" eventPoll.
     *
     * @param id the id of the eventPollDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/event-polls/{id}")
    @Timed
    public ResponseEntity<Void> deleteEventPoll(@PathVariable Long id) {
        log.debug("REST request to delete EventPoll : {}", id);
        eventPollService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("eventPoll", id.toString())).build();
    }

    /**
     * SEARCH  /_search/event-polls?query=:query : search for the eventPoll corresponding
     * to the query.
     *
     * @param query the query of the eventPoll search 
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/event-polls")
    @Timed
    public ResponseEntity<List<EventPollDTO>> searchEventPolls(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of EventPolls for query {}", query);
        Page<EventPollDTO> page = eventPollService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/event-polls");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
