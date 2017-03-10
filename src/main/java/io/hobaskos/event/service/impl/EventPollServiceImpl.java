package io.hobaskos.event.service.impl;

import io.hobaskos.event.service.EventPollService;
import io.hobaskos.event.domain.EventPoll;
import io.hobaskos.event.repository.EventPollRepository;
import io.hobaskos.event.repository.search.EventPollSearchRepository;
import io.hobaskos.event.service.dto.EventPollDTO;
import io.hobaskos.event.service.mapper.EventPollMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing EventPoll.
 */
@Service
@Transactional
public class EventPollServiceImpl implements EventPollService{

    private final Logger log = LoggerFactory.getLogger(EventPollServiceImpl.class);
    
    @Inject
    private EventPollRepository eventPollRepository;

    @Inject
    private EventPollMapper eventPollMapper;

    @Inject
    private EventPollSearchRepository eventPollSearchRepository;

    /**
     * Save a eventPoll.
     *
     * @param eventPollDTO the entity to save
     * @return the persisted entity
     */
    public EventPollDTO save(EventPollDTO eventPollDTO) {
        log.debug("Request to save EventPoll : {}", eventPollDTO);
        EventPoll eventPoll = eventPollMapper.eventPollDTOToEventPoll(eventPollDTO);
        eventPoll = eventPollRepository.save(eventPoll);
        EventPollDTO result = eventPollMapper.eventPollToEventPollDTO(eventPoll);
        eventPollSearchRepository.save(eventPoll);
        return result;
    }

    /**
     *  Get all the eventPolls.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true) 
    public Page<EventPollDTO> findAll(Pageable pageable) {
        log.debug("Request to get all EventPolls");
        Page<EventPoll> result = eventPollRepository.findAll(pageable);
        return result.map(eventPoll -> eventPollMapper.eventPollToEventPollDTO(eventPoll));
    }

    /**
     *  Get one eventPoll by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true) 
    public EventPollDTO findOne(Long id) {
        log.debug("Request to get EventPoll : {}", id);
        EventPoll eventPoll = eventPollRepository.findOne(id);
        EventPollDTO eventPollDTO = eventPollMapper.eventPollToEventPollDTO(eventPoll);
        return eventPollDTO;
    }

    /**
     *  Delete the  eventPoll by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete EventPoll : {}", id);
        eventPollRepository.delete(id);
        eventPollSearchRepository.delete(id);
    }

    /**
     * Search for the eventPoll corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EventPollDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of EventPolls for query {}", query);
        Page<EventPoll> result = eventPollSearchRepository.search(queryStringQuery(query), pageable);
        return result.map(eventPoll -> eventPollMapper.eventPollToEventPollDTO(eventPoll));
    }
}
