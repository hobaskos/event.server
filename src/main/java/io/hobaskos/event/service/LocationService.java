package io.hobaskos.event.service;

import io.hobaskos.event.service.dto.LocationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Service Interface for managing Location.
 */
public interface LocationService {

    /**
     * Save a location.
     *
     * @param locationDTO the entity to save
     * @return the persisted entity
     */
    LocationDTO save(LocationDTO locationDTO);

    /**
     *  Get all the locations.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<LocationDTO> findAll(Pageable pageable);

    /**
     *  Get the "id" location.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    LocationDTO findOne(Long id);

    /**
     *  Delete the "id" location.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the location corresponding to the query.
     *
     *  @param query the query of the search
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<LocationDTO> search(String query, Pageable pageable);

    /**
     * Search for locations nearby geoPoint
     * @param lat
     * @param lon
     * @param description
     * @param pageable
     * @return the list of entities
     */
    Page<LocationDTO> searchNearby(Double lat, Double lon, String description, Pageable pageable);
}
