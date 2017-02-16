package io.hobaskos.event.service.impl;

import io.hobaskos.event.domain.EventCategory;
import io.hobaskos.event.domain.Location;
import io.hobaskos.event.repository.search.LocationSearchRepository;
import io.hobaskos.event.service.EventService;
import io.hobaskos.event.domain.Event;
import io.hobaskos.event.repository.EventRepository;
import io.hobaskos.event.repository.search.EventSearchRepository;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.dto.EventDTO;
import io.hobaskos.event.service.mapper.EventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.security.access.method.P;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Event.
 */
@Service
@Transactional
public class EventServiceImpl implements EventService{

    private final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    @Inject
    private EventRepository eventRepository;

    @Inject
    private EventMapper eventMapper;

    @Inject
    private EventSearchRepository eventSearchRepository;

    @Inject
    private LocationSearchRepository locationSearchRepository;

    @Inject
    private UserService userService;

    /**
     * Save a event.
     *
     * @param eventDTO the entity to save
     * @return the persisted entity
     */
    public EventDTO save(EventDTO eventDTO) {
        log.debug("Request to save Event : {}", eventDTO);
        Event event = eventMapper.eventDTOToEvent(eventDTO);
        event.setOwner(userService.getUserWithAuthorities());
        event = eventRepository.save(event);
        EventDTO result = eventMapper.eventToEventDTO(event);
        eventSearchRepository.save(event);
        return result;
    }

    /**
     *  Get all the events.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EventDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Events");
        Page<Event> result = eventRepository.findAll(pageable);
        return result.map(event -> eventMapper.eventToEventDTO(event));
    }

    /**
     * Get all events with the current event category
     * @param eventCategory
     * @param pageable
     * @return the list of entities
     */
    public Page<EventDTO> findAllWithEventCategory(EventCategory eventCategory, Pageable pageable) {
        log.debug("Request to get events with category {}", eventCategory.getTitle());
        Page<Event> result = eventRepository.findByEventCategory(eventCategory, pageable);
        return result.map(event -> eventMapper.eventToEventDTO(event));
    }

    /**
     *  Get one event by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public EventDTO findOne(Long id) {
        log.debug("Request to get Event : {}", id);
        Event event = eventRepository.findOne(id);
        EventDTO eventDTO = eventMapper.eventToEventDTO(event);
        return eventDTO;
    }

    /**
     *  Delete the  event by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Event : {}", id);
        eventRepository.delete(id);
        eventSearchRepository.delete(id);
    }

    /**
     * Search for the event corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EventDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Events for query {}", query);
        Page<Event> result = eventSearchRepository.search(queryStringQuery(query), pageable);
        return result.map(event -> eventMapper.eventToEventDTO(event));
    }

    /**
     * Search for nearby events - quite expensive due to missing ability to search directly for "locations.geoPoint"
     * The current implementation is a bit dirty since its dependant on both Elastic and the JpaRepositories (SQL).
     *
     * The original implementation of this was:
     *         NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
     *            .withPageable(pageable)
     *            .withQuery(geoDistanceQuery("locations.geoPoint").lat(lat).lon(lon).distance(distance));
     *
     * @param lat
     * @param lon
     * @param distance
     * @param pageable
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EventDTO> searchNearby(Double lat, Double lon, String distance,
                                       LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        log.debug("Request to search for a page of nearby Events lat:{},lon:{},distance:{}", lat, lon, distance);

        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
            .withPageable(pageable)
            .withQuery(boolQuery()
                .must(rangeQuery("fromDate").gte(fromDate).queryName("toDate").lte(toDate))
                .filter(geoDistanceQuery("geoPoint").lat(lat).lon(lon).distance(distance))
            );

        //First find the location using elastic
        Iterable<Location> locations = locationSearchRepository.search(searchQueryBuilder.build());

        //Then find the events connected to these locations using the ordinary JpaRepositories.
        Page<Event> result = eventRepository.findByLocationsIn(StreamSupport.stream(locations.spliterator(), false)
                .collect(Collectors.toSet()), pageable);
        return result.map(event -> eventMapper.eventToEventDTO(event));
    }
}
