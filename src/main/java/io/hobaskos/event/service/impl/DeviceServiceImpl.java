package io.hobaskos.event.service.impl;

import io.hobaskos.event.domain.User;
import io.hobaskos.event.service.DeviceService;
import io.hobaskos.event.domain.Device;
import io.hobaskos.event.repository.DeviceRepository;
import io.hobaskos.event.repository.search.DeviceSearchRepository;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.dto.DeviceDTO;
import io.hobaskos.event.service.mapper.DeviceMapper;
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
 * Service Implementation for managing Device.
 */
@Service
@Transactional
public class DeviceServiceImpl implements DeviceService{

    private final Logger log = LoggerFactory.getLogger(DeviceServiceImpl.class);

    @Inject
    private DeviceRepository deviceRepository;

    @Inject
    private DeviceMapper deviceMapper;

    @Inject
    private DeviceSearchRepository deviceSearchRepository;

    @Inject
    private UserService userService;

    /**
     * Save a device.
     *
     * @param deviceDTO the entity to save
     * @return the persisted entity
     */
    public DeviceDTO save(DeviceDTO deviceDTO) {
        log.debug("Request to save Device : {}", deviceDTO);
        Device device = deviceMapper.deviceDTOToDevice(deviceDTO);
        device.setUser(userService.getUserWithAuthorities());
        device = deviceRepository.save(device);
        DeviceDTO result = deviceMapper.deviceToDeviceDTO(device);
        deviceSearchRepository.save(device);
        return result;
    }

    /**
     *  Get all the devices.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<DeviceDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Devices");
        Page<Device> result = deviceRepository.findAll(pageable);
        return result.map(device -> deviceMapper.deviceToDeviceDTO(device));
    }

    /**
     * Find all devices for user
     * @param user
     * @param pageable
     * @return the list of entities
     */
    public Page<DeviceDTO> findAllForUser(User user, Pageable pageable) {
        log.debug("Request to get all Devices for user {}", user.getLogin());
        Page<Device> result = deviceRepository.findByUserIsCurrentUser(pageable);
        return result.map(device -> deviceMapper.deviceToDeviceDTO(device));
    }

    /**
     *  Get one device by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public DeviceDTO findOne(Long id) {
        log.debug("Request to get Device : {}", id);
        Device device = deviceRepository.findOne(id);
        return deviceMapper.deviceToDeviceDTO(device);
    }

    /**
     *  Delete the  device by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Device : {}", id);
        deviceRepository.delete(id);
        deviceSearchRepository.delete(id);
    }

    /**
     * Search for the device corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<DeviceDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Devices for query {}", query);
        Page<Device> result = deviceSearchRepository.search(queryStringQuery(query), pageable);
        return result.map(device -> deviceMapper.deviceToDeviceDTO(device));
    }
}
