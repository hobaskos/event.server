package io.hobaskos.event.service.impl;

import io.hobaskos.event.domain.EventCategory;
import io.hobaskos.event.domain.EventPoll;
import io.hobaskos.event.domain.enumeration.EventPollStatus;
import io.hobaskos.event.service.EventPollService;
import io.hobaskos.event.service.EventService;
import io.hobaskos.event.domain.Event;
import io.hobaskos.event.repository.EventRepository;
import io.hobaskos.event.repository.search.EventSearchRepository;
import io.hobaskos.event.service.StorageService;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.dto.EventDTO;
import io.hobaskos.event.service.dto.EventPollDTO;
import io.hobaskos.event.service.mapper.EventMapper;
import io.hobaskos.event.service.util.ContentTypeUtil;
import io.hobaskos.event.service.util.RandomUtil;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Inject
    private StorageService storageService;

    @Inject
    private EventPollService eventPollService;

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

        if (event.getId() != null) { // done to keep elastic happy...
            Event originEvent = eventRepository.findOneWithEagerRelations(event.getId());
            event.setAttendings(originEvent.getAttendings());
            event.setPolls(originEvent.getPolls());
            event.setLocations(originEvent.getLocations());
        }

        if (event.isPrivateEvent() && event.getInvitationCode() == null){
            event.setInvitationCode(RandomUtil.generateRandomInviteCode());
        }


        log.debug("eventDTO image check");
        if (eventDTO.getImage() != null && eventDTO.getImageContentType() != null) {
            log.debug("Trying to save image");
            String filename = storageService.store(eventDTO.getImage(),
                ContentTypeUtil.defineImageName(eventDTO.getImageContentType()));
            event.setImageUrl("/files/" + filename);
        }

        event = eventRepository.save(event);
        eventSearchRepository.save(event);

        EventPollDTO poll = new EventPollDTO();
        poll.setEventId(event.getId());
        poll.setTitle("Default poll for " + event.getTitle());
        poll.setStatus(EventPollStatus.ACTIVE);
        eventPollService.save(poll);

        return eventMapper.eventToEventDTO(event);
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
     * Get one event by invite code
     * @param inviteCode
     * @return
     */
    public Optional<EventDTO> findOneByInviteCode(String inviteCode) {
        return eventRepository.findOneWithEagerRelations(inviteCode)
            .map(eventMapper::eventToEventDTO);
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
        BoolQueryBuilder queryBuilder = boolQuery()
            .mustNot(termQuery("privateEvent", true))
            .must(queryStringQuery(query));
        Page<Event> result = eventSearchRepository.search(new NativeSearchQueryBuilder()
            .withPageable(pageable)
            .withQuery(queryBuilder)
            .build());
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
    public Page<EventDTO> searchNearby(String query, Double lat, Double lon, String distance,
                                       LocalDateTime fromDate, LocalDateTime toDate,
                                       List<Long> eventCategoryIds,
                                       Pageable pageable) {
        log.debug("Request to search for a page of nearby Events query:{},lat:{},lon:{},distance:{},categories:{}",
            query, lat, lon, distance, eventCategoryIds);

        BoolQueryBuilder queryBuilder = boolQuery()
            .mustNot(termQuery("privateEvent", true))
            .must(rangeQuery("fromDate").gte(fromDate).queryName("toDate").lte(toDate))
            .filter(termsQuery("eventCategory.id", eventCategoryIds))
            .filter(nestedQuery("locations", geoDistanceQuery("locations.geoPoint")
                .lat(lat).lon(lon).distance(distance)));

        if (query != null && query.length() > 2) { queryBuilder.must(queryStringQuery(query)); }

        Page<Event> page = eventSearchRepository.search(new NativeSearchQueryBuilder()
            .withPageable(pageable)
            .withQuery(queryBuilder)
            .build());
        return page.map(event -> eventMapper.eventToEventDTO(event));
    }
}
