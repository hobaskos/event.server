package io.hobaskos.event.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.hobaskos.event.service.UserConnectionService;
import io.hobaskos.event.web.rest.util.HeaderUtil;
import io.hobaskos.event.web.rest.util.PaginationUtil;
import io.hobaskos.event.service.dto.UserConnectionDTO;

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
 * REST controller for managing UserConnection.
 */
@RestController
@RequestMapping("/api")
public class UserConnectionResource {

    private final Logger log = LoggerFactory.getLogger(UserConnectionResource.class);
        
    @Inject
    private UserConnectionService userConnectionService;

    /**
     * POST  /user-connections : Create a new userConnection.
     *
     * @param userConnectionDTO the userConnectionDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new userConnectionDTO, or with status 400 (Bad Request) if the userConnection has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/user-connections")
    @Timed
    public ResponseEntity<UserConnectionDTO> createUserConnection(@Valid @RequestBody UserConnectionDTO userConnectionDTO) throws URISyntaxException {
        log.debug("REST request to save UserConnection : {}", userConnectionDTO);
        if (userConnectionDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("userConnection", "idexists", "A new userConnection cannot already have an ID")).body(null);
        }
        UserConnectionDTO result = userConnectionService.save(userConnectionDTO);
        return ResponseEntity.created(new URI("/api/user-connections/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("userConnection", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /user-connections : Updates an existing userConnection.
     *
     * @param userConnectionDTO the userConnectionDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated userConnectionDTO,
     * or with status 400 (Bad Request) if the userConnectionDTO is not valid,
     * or with status 500 (Internal Server Error) if the userConnectionDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/user-connections")
    @Timed
    public ResponseEntity<UserConnectionDTO> updateUserConnection(@Valid @RequestBody UserConnectionDTO userConnectionDTO) throws URISyntaxException {
        log.debug("REST request to update UserConnection : {}", userConnectionDTO);
        if (userConnectionDTO.getId() == null) {
            return createUserConnection(userConnectionDTO);
        }
        UserConnectionDTO result = userConnectionService.save(userConnectionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("userConnection", userConnectionDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /user-connections : get all the userConnections.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of userConnections in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/user-connections")
    @Timed
    public ResponseEntity<List<UserConnectionDTO>> getAllUserConnections(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of UserConnections");
        Page<UserConnectionDTO> page = userConnectionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/user-connections");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /user-connections/:id : get the "id" userConnection.
     *
     * @param id the id of the userConnectionDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the userConnectionDTO, or with status 404 (Not Found)
     */
    @GetMapping("/user-connections/{id}")
    @Timed
    public ResponseEntity<UserConnectionDTO> getUserConnection(@PathVariable Long id) {
        log.debug("REST request to get UserConnection : {}", id);
        UserConnectionDTO userConnectionDTO = userConnectionService.findOne(id);
        return Optional.ofNullable(userConnectionDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /user-connections/:id : delete the "id" userConnection.
     *
     * @param id the id of the userConnectionDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/user-connections/{id}")
    @Timed
    public ResponseEntity<Void> deleteUserConnection(@PathVariable Long id) {
        log.debug("REST request to delete UserConnection : {}", id);
        userConnectionService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("userConnection", id.toString())).build();
    }

    /**
     * SEARCH  /_search/user-connections?query=:query : search for the userConnection corresponding
     * to the query.
     *
     * @param query the query of the userConnection search 
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/user-connections")
    @Timed
    public ResponseEntity<List<UserConnectionDTO>> searchUserConnections(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of UserConnections for query {}", query);
        Page<UserConnectionDTO> page = userConnectionService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/user-connections");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
