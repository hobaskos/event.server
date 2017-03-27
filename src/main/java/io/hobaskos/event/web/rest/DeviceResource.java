package io.hobaskos.event.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.hobaskos.event.security.AuthoritiesConstants;
import io.hobaskos.event.service.DeviceService;
import io.hobaskos.event.web.rest.util.HeaderUtil;
import io.hobaskos.event.web.rest.util.PaginationUtil;
import io.hobaskos.event.service.dto.DeviceDTO;

import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Device.
 */
@RestController
@RequestMapping("/api")
public class DeviceResource {

    private final Logger log = LoggerFactory.getLogger(DeviceResource.class);

    @Inject
    private DeviceService deviceService;

    /**
     * GET  /devices : get all the devices.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of devices in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/devices")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<List<DeviceDTO>> getAllDevices(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Devices");
        Page<DeviceDTO> page = deviceService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/devices");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /devices/:id : get the "id" device.
     *
     * @param id the id of the deviceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the deviceDTO, or with status 404 (Not Found)
     */
    @GetMapping("/devices/{id}")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<DeviceDTO> getDevice(@PathVariable Long id) {
        log.debug("REST request to get Device : {}", id);
        DeviceDTO deviceDTO = deviceService.findOne(id);
        return Optional.ofNullable(deviceDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /devices/:id : delete the "id" device.
     *
     * @param id the id of the deviceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/devices/{id}")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        log.debug("REST request to delete Device : {}", id);
        deviceService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("device", id.toString())).build();
    }

    /**
     * SEARCH  /_search/devices?query=:query : search for the device corresponding
     * to the query.
     *
     * @param query the query of the device search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/devices")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<List<DeviceDTO>> searchDevices(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Devices for query {}", query);
        Page<DeviceDTO> page = deviceService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/devices");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
