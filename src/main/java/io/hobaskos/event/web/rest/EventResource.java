package io.hobaskos.event.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.hobaskos.event.domain.EventCategory;
import io.hobaskos.event.repository.EventCategoryRepository;
import io.hobaskos.event.service.EventService;
import io.hobaskos.event.web.rest.util.HeaderUtil;
import io.hobaskos.event.web.rest.util.PaginationUtil;
import io.hobaskos.event.service.dto.EventDTO;

import io.reactivex.exceptions.Exceptions;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for managing Event.
 */
@RestController
@RequestMapping("/api")
public class EventResource {

    private final Logger log = LoggerFactory.getLogger(EventResource.class);
    private final static int DEFAULT_DAYS_FORWARD = 14;
    private final static int DEFAULT_DAYS_BACKWARD = 1;

    @Inject
    private EventService eventService;

    @Inject
    private EventCategoryRepository eventCategoryRepository;

    /**
     * POST  /events : Create a new event.
     *
     * @param eventDTO the eventDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new eventDTO, or with status 400 (Bad Request) if the event has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/events")
    @Timed
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO eventDTO) throws URISyntaxException {
        log.debug("REST request to save Event : {}", eventDTO);
        if (eventDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("event", "idexists", "A new event cannot already have an ID")).body(null);
        }
        EventDTO result = eventService.save(eventDTO);
        return ResponseEntity.created(new URI("/api/events/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("event", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /events : Updates an existing event.
     *
     * @param eventDTO the eventDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated eventDTO,
     * or with status 400 (Bad Request) if the eventDTO is not valid,
     * or with status 500 (Internal Server Error) if the eventDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/events")
    @Timed
    public ResponseEntity<EventDTO> updateEvent(@Valid @RequestBody EventDTO eventDTO) throws URISyntaxException {
        log.debug("REST request to update Event : {}", eventDTO);
        if (eventDTO.getId() == null) {
            return createEvent(eventDTO);
        }
        EventDTO result = eventService.save(eventDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("event", eventDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /events : get all the events.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of events in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/events")
    @Timed
    public ResponseEntity<List<EventDTO>> getAllEvents(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Events");
        Page<EventDTO> page = eventService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/events");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET /event-categories/{id}/events : get all events with event id
     * @param id
     * @param pageable
     * @return the list of events.
     */
    @GetMapping("/event-categories/{id}/events")
    @Timed
    public ResponseEntity<List<EventDTO>> getAllEventsWithCategory(@PathVariable Long id, @ApiParam Pageable pageable) {
        EventCategory eventCategory = eventCategoryRepository.findOne(id);
        return Optional.ofNullable(eventCategory)
            .map(result -> {
                try {
                    Page<EventDTO> page = eventService.findAllWithEventCategory(eventCategory, pageable);
                    HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/event-categories/" + id + "/events");
                    return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
                } catch (URISyntaxException e) { throw Exceptions.propagate(e); }
            })
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET  /events/:id : get the "id" event.
     *
     * @param id the id of the eventDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the eventDTO, or with status 404 (Not Found)
     */
    @GetMapping("/events/{id}")
    @Timed
    public ResponseEntity<EventDTO> getEvent(@PathVariable Long id) {
        log.debug("REST request to get Event : {}", id);
        EventDTO eventDTO = eventService.findOne(id);
        return Optional.ofNullable(eventDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /events/:id : delete the "id" event.
     *
     * @param id the id of the eventDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/events/{id}")
    @Timed
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        log.debug("REST request to delete Event : {}", id);
        eventService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("event", id.toString())).build();
    }

    /**
     * SEARCH  /_search/events?query=:query : search for the event corresponding
     * to the query.
     *
     * @param query the query of the event search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/events")
    @Timed
    public ResponseEntity<List<EventDTO>> searchEvents(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Events for query {}", query);
        Page<EventDTO> page = eventService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/events");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Search /_search/events-nearby : search for events nearby corresponding to the lat,lon and distance params
     * @param lat
     * @param lon
     * @param distance
     * @param pageable
     * @return the result of the search
     * @throws URISyntaxException
     */
    @GetMapping("/_search/events-nearby")
    @Timed
    public ResponseEntity<List<EventDTO>> searchEventsNearby(@RequestParam(required = false) String query,
                                                             @RequestParam Double lat,
                                                             @RequestParam Double lon,
                                                             @RequestParam String distance,
                                                             @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Date fromDate,
                                                             @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Date toDate,
                                                             @RequestParam(required=false) Set<Long> categories,
                                                             @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Events for nearby query {}");
        LocalDateTime fromDateLocal = fromDate != null ?
                fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now().minusDays(DEFAULT_DAYS_BACKWARD);
        LocalDateTime toDateLocal = toDate != null ?
                toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now().plusDays(DEFAULT_DAYS_FORWARD);
        Set<EventCategory> eventCategories = getEventCategoriesFromIntegerSet(categories);
        Page<EventDTO> page = eventService.searchNearby(query, lat, lon, distance, fromDateLocal, toDateLocal, eventCategories, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(distance, page, "/api/_search/events-nearby");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    private Set<EventCategory> getEventCategoriesFromIntegerSet(Set<Long> eventCategories) {
        if (eventCategories == null || eventCategories.size() == 0) {
            return eventCategoryRepository.findAll().stream().collect(Collectors.toSet());
        }

        Set<EventCategory> matchedEventsCategories = eventCategoryRepository.findById(eventCategories);
        if (matchedEventsCategories.size() != 0) {
            return matchedEventsCategories;
        } else {
            return eventCategoryRepository.findAll().stream().collect(Collectors.toSet());
        }
    }
}
