package io.hobaskos.event.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.hobaskos.event.service.EventService;
import io.hobaskos.event.service.LocationService;
import io.hobaskos.event.web.rest.util.HeaderUtil;
import io.hobaskos.event.web.rest.util.PaginationUtil;
import io.hobaskos.event.service.dto.LocationDTO;

import io.reactivex.exceptions.Exceptions;
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
 * REST controller for managing Location.
 */
@RestController
@RequestMapping("/api")
public class LocationResource {

    private final Logger log = LoggerFactory.getLogger(LocationResource.class);

    @Inject
    private LocationService locationService;

    @Inject
    private EventService eventService;

    /**
     * POST  /locations : Create a new location.
     *
     * @param locationDTO the locationDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new locationDTO, or with status 400 (Bad Request) if the location has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/locations")
    @Timed
    public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody LocationDTO locationDTO) throws URISyntaxException {
        log.debug("REST request to save Location : {}", locationDTO);
        if (locationDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("location", "idexists", "A new location cannot already have an ID")).body(null);
        }
        LocationDTO result = locationService.save(locationDTO);
        return ResponseEntity.created(new URI("/api/locations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("location", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /locations : Updates an existing location.
     *
     * @param locationDTO the locationDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated locationDTO,
     * or with status 400 (Bad Request) if the locationDTO is not valid,
     * or with status 500 (Internal Server Error) if the locationDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/locations")
    @Timed
    public ResponseEntity<LocationDTO> updateLocation(@Valid @RequestBody LocationDTO locationDTO) throws URISyntaxException {
        log.debug("REST request to update Location : {}", locationDTO);
        if (locationDTO.getId() == null) {
            return createLocation(locationDTO);
        }
        LocationDTO result = locationService.save(locationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("location", locationDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /locations : get all the locations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of locations in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/locations")
    @Timed
    public ResponseEntity<List<LocationDTO>> getAllLocations(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Locations");
        Page<LocationDTO> page = locationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/locations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /locations/:id : get the "id" location.
     *
     * @param id the id of the locationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the locationDTO, or with status 404 (Not Found)
     */
    @GetMapping("/locations/{id}")
    @Timed
    public ResponseEntity<LocationDTO> getLocation(@PathVariable Long id) {
        log.debug("REST request to get Location : {}", id);
        LocationDTO locationDTO = locationService.findOne(id);
        return Optional.ofNullable(locationDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /locations/:id : delete the "id" location.
     *
     * @param id the id of the locationDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/locations/{id}")
    @Timed
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        log.debug("REST request to delete Location : {}", id);
        locationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("location", id.toString())).build();
    }

    /**
     * SEARCH  /_search/locations?query=:query : search for the location corresponding
     * to the query.
     *
     * @param query the query of the location search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/locations")
    @Timed
    public ResponseEntity<List<LocationDTO>> searchLocations(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Locations for query {}", query);
        Page<LocationDTO> page = locationService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/locations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Get locations nearby
     * @param lat
     * @param lon
     * @param distance
     * @param pageable
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/_search/locations-nearby")
    @Timed
    public ResponseEntity<List<LocationDTO>> searchLocationNearby(@RequestParam Double lat,
                                                                  @RequestParam Double lon,
                                                                  @RequestParam String distance,
                                                                  @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of nearby Locations lat:{},lon:{},distance{}", lat,lon,distance);
        Page<LocationDTO> page = locationService.searchNearby(lat, lon, distance, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(distance, page, "/api/_search/locations-nearby");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Get locations connected to an event
     * @param id
     * @param pageable
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/events/{id}/locations")
    @Timed
    public ResponseEntity<List<LocationDTO>> getLocationsForEvent(@PathVariable Long id, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get locations for event id: {}", id);
        return Optional.ofNullable(eventService.findOne(id))
            .map(eventDTO -> {
                try {
                    Page<LocationDTO> page = locationService.getLocationsWithEvent(id, pageable);
                    HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/events/"+id+"/locations");
                    return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
                } catch (URISyntaxException use) { throw Exceptions.propagate(use); }
            })
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
