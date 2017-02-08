package io.hobaskos.event.service.impl;

import io.hobaskos.event.service.EventService;
import io.hobaskos.event.domain.Event;
import io.hobaskos.event.repository.EventRepository;
import io.hobaskos.event.repository.search.EventSearchRepository;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.dto.EventDTO;
import io.hobaskos.event.service.mapper.EventMapper;
import org.elasticsearch.common.geo.GeoPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.util.Date;

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
    public Page<EventDTO> search(String query, GeoPoint geoPoint, String distance,
                                 Date fromDate, Date toDate, Pageable pageable) {
        log.debug("Request to search for a page of Events for query {}", query);

        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
            .withPageable(pageable)
            .withQuery(boolQuery()
                .must(queryStringQuery(query))
                .should(rangeQuery("locations.fromDate").gte(fromDate).queryName("locations.toDate").lte(toDate))
                .should(geoDistanceQuery("locations.geoPoint")
                    .lat(geoPoint.lat())
                    .lon(geoPoint.lon())
                    .distance(distance)));

        Page<Event> result = eventSearchRepository.search(searchQueryBuilder.build());
        return result.map(event -> eventMapper.eventToEventDTO(event));
    }
}
