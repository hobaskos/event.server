package io.hobaskos.event.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.hobaskos.event.service.EventImageService;
import io.hobaskos.event.web.rest.util.HeaderUtil;
import io.hobaskos.event.web.rest.util.PaginationUtil;
import io.hobaskos.event.service.dto.EventImageDTO;

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
 * REST controller for managing EventImage.
 */
@RestController
@RequestMapping("/api")
public class EventImageResource {

    private final Logger log = LoggerFactory.getLogger(EventImageResource.class);

    @Inject
    private EventImageService eventImageService;

    /**
     * POST  /event-images : Create a new eventImage.
     *
     * @param eventImageDTO the eventImageDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new eventImageDTO, or with status 400 (Bad Request) if the eventImage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/event-images")
    @Timed
    public ResponseEntity<EventImageDTO> createEventImage(@Valid @RequestBody EventImageDTO eventImageDTO) throws URISyntaxException {
        log.debug("REST request to save EventImage : {}", eventImageDTO);
        if (eventImageDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("eventImage", "idexists", "A new eventImage cannot already have an ID")).body(null);
        }
        EventImageDTO result = eventImageService.save(eventImageDTO);
        return ResponseEntity.created(new URI("/api/event-images/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("eventImage", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /event-images : Updates an existing eventImage.
     *
     * @param eventImageDTO the eventImageDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated eventImageDTO,
     * or with status 400 (Bad Request) if the eventImageDTO is not valid,
     * or with status 500 (Internal Server Error) if the eventImageDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/event-images")
    @Timed
    public ResponseEntity<EventImageDTO> updateEventImage(@Valid @RequestBody EventImageDTO eventImageDTO) throws URISyntaxException {
        log.debug("REST request to update EventImage : {}", eventImageDTO);
        if (eventImageDTO.getId() == null) {
            return createEventImage(eventImageDTO);
        }
        EventImageDTO result = eventImageService.save(eventImageDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("eventImage", eventImageDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /event-images : get all the eventImages.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of eventImages in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/event-images")
    @Timed
    public ResponseEntity<List<EventImageDTO>> getAllEventImages(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of EventImages");
        Page<EventImageDTO> page = eventImageService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/event-images");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /event-polls/:id/event-images : get all the eventImages.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of eventImages in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/event-polls/{id}/event-images")
    @Timed
    public ResponseEntity<List<EventImageDTO>> getAllEventImagesForPoll(@PathVariable Long id)
        throws URISyntaxException {
        log.debug("REST request to get a page of EventImages");
        return eventImageService.findAllForEvent(id)
            .map(eventImageDTOS -> new ResponseEntity<>(eventImageDTOS, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET  /event-images/:id : get the "id" eventImage.
     *
     * @param id the id of the eventImageDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the eventImageDTO, or with status 404 (Not Found)
     */
    @GetMapping("/event-images/{id}")
    @Timed
    public ResponseEntity<EventImageDTO> getEventImage(@PathVariable Long id) {
        log.debug("REST request to get EventImage : {}", id);
        EventImageDTO eventImageDTO = eventImageService.findOne(id);
        return Optional.ofNullable(eventImageDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /event-images/:id : delete the "id" eventImage.
     *
     * @param id the id of the eventImageDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/event-images/{id}")
    @Timed
    public ResponseEntity<Void> deleteEventImage(@PathVariable Long id) {
        log.debug("REST request to delete EventImage : {}", id);
        eventImageService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("eventImage", id.toString())).build();
    }

    /**
     * SEARCH  /_search/event-images?query=:query : search for the eventImage corresponding
     * to the query.
     *
     * @param query the query of the eventImage search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/event-images")
    @Timed
    public ResponseEntity<List<EventImageDTO>> searchEventImages(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of EventImages for query {}", query);
        Page<EventImageDTO> page = eventImageService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/event-images");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
