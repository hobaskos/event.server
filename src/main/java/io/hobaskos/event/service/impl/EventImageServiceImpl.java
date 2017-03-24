package io.hobaskos.event.service.impl;

import io.hobaskos.event.repository.EventPollRepository;
import io.hobaskos.event.service.EventImageService;
import io.hobaskos.event.domain.EventImage;
import io.hobaskos.event.repository.EventImageRepository;
import io.hobaskos.event.repository.search.EventImageSearchRepository;
import io.hobaskos.event.service.StorageService;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.dto.EventImageDTO;
import io.hobaskos.event.service.mapper.EventImageMapper;
import io.hobaskos.event.service.util.ContentTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing EventImage.
 */
@Service
@Transactional
public class EventImageServiceImpl implements EventImageService{

    private final Logger log = LoggerFactory.getLogger(EventImageServiceImpl.class);

    @Inject
    private EventImageRepository eventImageRepository;

    @Inject
    private EventImageMapper eventImageMapper;

    @Inject
    private EventImageSearchRepository eventImageSearchRepository;

    @Inject
    private StorageService storageService;

    @Inject
    private UserService userService;

    @Inject
    private EventPollRepository eventPollRepository;

    /**
     * Save a eventImage.
     *
     * @param eventImageDTO the entity to save
     * @return the persisted entity
     */
    public EventImageDTO save(EventImageDTO eventImageDTO) {
        log.debug("Request to save EventImage : {}", eventImageDTO);
        EventImage eventImage = eventImageMapper.eventImageDTOToEventImage(eventImageDTO);

        if (eventImage.getId() != null) {
            EventImage originalEventImage = eventImageRepository.findOne(eventImage.getId());
            eventImage.setVoteCount(originalEventImage.getVoteCount());
        }

        if (eventImageDTO.getFile() != null && eventImageDTO.getFileContentType() != null) {
            String filename = storageService.store(eventImageDTO.getFile(),
                ContentTypeUtil.defineImageName(eventImageDTO.getFileContentType()));
            eventImage.setImageUrl("/files/" + filename);
        }

        eventImage.setUser(userService.getUserWithAuthorities());

        eventImage = eventImageRepository.save(eventImage);
        EventImageDTO result = eventImageMapper.eventImageToEventImageDTO(eventImage);
        eventImageSearchRepository.save(eventImage);
        return result;
    }

    /**
     *  Get all the eventImages.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EventImageDTO> findAll(Pageable pageable) {
        log.debug("Request to get all EventImages");
        Page<EventImage> result = eventImageRepository.findAll(pageable);
        return result.map(eventImage -> eventImageMapper.eventImageToEventImageDTO(eventImage));
    }

    /**
     * Get all eventImages for event-poll
     * @param id
     * @return
     */
    public Optional<List<EventImageDTO>> findAllForEvent(Long id) {
        return eventPollRepository.findOneById(id)
            .map(eventPoll -> eventImageMapper.eventImagesToEventImageDTOs(new ArrayList<>(eventPoll.getImages())));
    }

    /**
     *  Get one eventImage by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public EventImageDTO findOne(Long id) {
        log.debug("Request to get EventImage : {}", id);
        EventImage eventImage = eventImageRepository.findOne(id);
        EventImageDTO eventImageDTO = eventImageMapper.eventImageToEventImageDTO(eventImage);
        return eventImageDTO;
    }

    /**
     *  Delete the  eventImage by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete EventImage : {}", id);
        eventImageRepository.delete(id);
        eventImageSearchRepository.delete(id);
    }

    /**
     * Search for the eventImage corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EventImageDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of EventImages for query {}", query);
        Page<EventImage> result = eventImageSearchRepository.search(queryStringQuery(query), pageable);
        return result.map(eventImage -> eventImageMapper.eventImageToEventImageDTO(eventImage));
    }

    /**
     * Decrease the voteCount for video
     * @param videoId
     */
    @Async
    public synchronized void increaseVoteCount(Long videoId) {
        log.debug("Request to increase voteCount for video {}", videoId);
        Optional.ofNullable(eventImageRepository.findOne(videoId))
            .ifPresent(ei -> {
                ei.increaseVoteCount();
                EventImage eventImage = eventImageRepository.save(ei);
                eventImageSearchRepository.save(eventImage);
            });
    }

    /**
     * Decrease the voteCount for video
     * @param videoId
     */
    @Async
    public void decreaseVoteCount(Long videoId) {
        log.debug("Request to increase voteCount for video {}", videoId);
         Optional.ofNullable(eventImageRepository.findOne(videoId))
            .ifPresent(ei -> {
                ei.decreaseVoteCount();
                EventImage eventImage = eventImageRepository.save(ei);
                eventImageSearchRepository.save(eventImage);
            });
    }
}
