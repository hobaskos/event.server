package io.hobaskos.event.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.hobaskos.event.domain.EventImageVote;

import io.hobaskos.event.repository.EventImageVoteRepository;
import io.hobaskos.event.repository.search.EventImageVoteSearchRepository;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.web.rest.util.HeaderUtil;
import io.hobaskos.event.web.rest.util.PaginationUtil;
import io.hobaskos.event.service.dto.EventImageVoteDTO;
import io.hobaskos.event.service.mapper.EventImageVoteMapper;

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
 * REST controller for managing EventImageVote.
 */
@RestController
@RequestMapping("/api")
public class EventImageVoteResource {

    private final Logger log = LoggerFactory.getLogger(EventImageVoteResource.class);

    @Inject
    private EventImageVoteRepository eventImageVoteRepository;

    @Inject
    private EventImageVoteMapper eventImageVoteMapper;

    @Inject
    private EventImageVoteSearchRepository eventImageVoteSearchRepository;

    @Inject
    private UserService userService;

    /**
     * POST  /event-image-votes : Create a new eventImageVote.
     *
     * @param eventImageVoteDTO the eventImageVoteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new eventImageVoteDTO, or with status 400 (Bad Request) if the eventImageVote has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/event-image-votes")
    @Timed
    public ResponseEntity<EventImageVoteDTO> createEventImageVote(@Valid @RequestBody EventImageVoteDTO eventImageVoteDTO) throws URISyntaxException {
        log.debug("REST request to save EventImageVote : {}", eventImageVoteDTO);
        if (eventImageVoteDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("eventImageVote", "idexists", "A new eventImageVote cannot already have an ID")).body(null);
        }
        EventImageVote eventImageVote = eventImageVoteMapper.eventImageVoteDTOToEventImageVote(eventImageVoteDTO);
        eventImageVote.setUser(userService.getUserWithAuthorities());
        eventImageVote = eventImageVoteRepository.save(eventImageVote);
        EventImageVoteDTO result = eventImageVoteMapper.eventImageVoteToEventImageVoteDTO(eventImageVote);
        eventImageVoteSearchRepository.save(eventImageVote);
        return ResponseEntity.created(new URI("/api/event-image-votes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("eventImageVote", result.getId().toString()))
            .body(result);
    }

    /**
     * GET  /event-image-votes : get all the eventImageVotes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of eventImageVotes in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/event-image-votes")
    @Timed
    public ResponseEntity<List<EventImageVoteDTO>> getAllEventImageVotes(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of EventImageVotes");
        Page<EventImageVote> page = eventImageVoteRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/event-image-votes");
        return new ResponseEntity<>(eventImageVoteMapper.eventImageVotesToEventImageVoteDTOs(page.getContent()), headers, HttpStatus.OK);
    }

    /**
     * GET  /event-image-votes/:id : get the "id" eventImageVote.
     *
     * @param id the id of the eventImageVoteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the eventImageVoteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/event-image-votes/{id}")
    @Timed
    public ResponseEntity<EventImageVoteDTO> getEventImageVote(@PathVariable Long id) {
        log.debug("REST request to get EventImageVote : {}", id);
        EventImageVote eventImageVote = eventImageVoteRepository.findOne(id);
        EventImageVoteDTO eventImageVoteDTO = eventImageVoteMapper.eventImageVoteToEventImageVoteDTO(eventImageVote);
        return Optional.ofNullable(eventImageVoteDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /event-image-votes/:id : delete the "id" eventImageVote.
     *
     * @param id the id of the eventImageVoteDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/event-image-votes/{id}")
    @Timed
    public ResponseEntity<Void> deleteEventImageVote(@PathVariable Long id) {
        log.debug("REST request to delete EventImageVote : {}", id);
        eventImageVoteRepository.delete(id);
        eventImageVoteSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("eventImageVote", id.toString())).build();
    }

    /**
     * SEARCH  /_search/event-image-votes?query=:query : search for the eventImageVote corresponding
     * to the query.
     *
     * @param query the query of the eventImageVote search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/event-image-votes")
    @Timed
    public ResponseEntity<List<EventImageVoteDTO>> searchEventImageVotes(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of EventImageVotes for query {}", query);
        Page<EventImageVote> page = eventImageVoteSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/event-image-votes");
        return new ResponseEntity<>(eventImageVoteMapper.eventImageVotesToEventImageVoteDTOs(page.getContent()), headers, HttpStatus.OK);
    }


}
