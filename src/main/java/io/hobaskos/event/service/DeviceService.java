package io.hobaskos.event.service;

import io.hobaskos.event.domain.User;
import io.hobaskos.event.service.dto.DeviceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Service Interface for managing Device.
 */
public interface DeviceService {

    /**
     * Save a device.
     *
     * @param deviceDTO the entity to save
     * @return the persisted entity
     */
    DeviceDTO save(DeviceDTO deviceDTO);

    /**
     *  Get all the devices.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<DeviceDTO> findAll(Pageable pageable);

    /**
     * Find all devices for user
     * @param user
     * @param pageable
     * @return the list of entities
     */
    Page<DeviceDTO> findAllForUser(User user, Pageable pageable);

    /**
     *  Get the "id" device.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    DeviceDTO findOne(Long id);

    /**
     *  Delete the "id" device.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the device corresponding to the query.
     *
     *  @param query the query of the search
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<DeviceDTO> search(String query, Pageable pageable);
}
