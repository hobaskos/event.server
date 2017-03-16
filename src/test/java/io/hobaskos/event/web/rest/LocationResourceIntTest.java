package io.hobaskos.event.web.rest;

import io.hobaskos.event.BackendApp;

import io.hobaskos.event.domain.Location;
import io.hobaskos.event.domain.Event;
import io.hobaskos.event.repository.LocationRepository;
import io.hobaskos.event.service.LocationService;
import io.hobaskos.event.repository.search.LocationSearchRepository;
import io.hobaskos.event.service.dto.LocationDTO;
import io.hobaskos.event.service.mapper.LocationMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.List;

import static io.hobaskos.event.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the LocationResource REST controller.
 *
 * @see LocationResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApp.class)
public class LocationResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_SEARCH_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SEARCH_NAME = "BBBBBBBBBB";

    private static final Double DEFAULT_LAT = -90D;
    private static final Double UPDATED_LAT = -89D;

    private static final Double DEFAULT_LON = -180D;
    private static final Double UPDATED_LON = -179D;

    private static final ZonedDateTime DEFAULT_FROM_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_FROM_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_TO_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_TO_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private LocationMapper locationMapper;

    @Inject
    private LocationService locationService;

    @Inject
    private LocationSearchRepository locationSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restLocationMockMvc;

    private Location location;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        LocationResource locationResource = new LocationResource();
        ReflectionTestUtils.setField(locationResource, "locationService", locationService);
        this.restLocationMockMvc = MockMvcBuilders.standaloneSetup(locationResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Location createEntity(EntityManager em) {
        Location location = new Location()
                .name(DEFAULT_NAME)
                .description(DEFAULT_DESCRIPTION)
                .address(DEFAULT_ADDRESS)
                .searchName(DEFAULT_SEARCH_NAME)
                .geoPoint(new GeoPoint(DEFAULT_LAT, DEFAULT_LON))
                .fromDate(DEFAULT_FROM_DATE)
                .toDate(DEFAULT_TO_DATE);
        // Add required entity
        Event event = EventResourceIntTest.createEntity(em);
        em.persist(event);
        em.flush();
        location.setEvent(event);
        return location;
    }

    @Before
    public void initTest() {
        locationSearchRepository.deleteAll();
        location = createEntity(em);
    }

    @Test
    @Transactional
    public void createLocation() throws Exception {
        int databaseSizeBeforeCreate = locationRepository.findAll().size();

        // Create the Location
        LocationDTO locationDTO = locationMapper.locationToLocationDTO(location);

        restLocationMockMvc.perform(post("/api/locations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(locationDTO)))
            .andExpect(status().isCreated());

        // Validate the Location in the database
        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeCreate + 1);
        Location testLocation = locationList.get(locationList.size() - 1);
        assertThat(testLocation.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testLocation.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testLocation.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testLocation.getSearchName()).isEqualTo(DEFAULT_SEARCH_NAME);
        assertThat(testLocation.getGeoPoint().getLat()).isEqualTo(DEFAULT_LAT);
        assertThat(testLocation.getGeoPoint().getLon()).isEqualTo(DEFAULT_LON);
        assertThat(testLocation.getFromDate()).isEqualTo(DEFAULT_FROM_DATE);
        assertThat(testLocation.getToDate()).isEqualTo(DEFAULT_TO_DATE);

        // Validate the Location in ElasticSearch
        Location locationEs = locationSearchRepository.findOne(testLocation.getId());
        assertThat(locationEs.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(locationEs.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(locationEs.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(locationEs.getSearchName()).isEqualTo(DEFAULT_SEARCH_NAME);
        assertThat(locationEs.getGeoPoint().getLat()).isEqualTo(DEFAULT_LAT);
        assertThat(locationEs.getGeoPoint().getLon()).isEqualTo(DEFAULT_LON);
        assertThat(locationEs.getFromDate()).isEqualTo(DEFAULT_FROM_DATE);
        assertThat(locationEs.getToDate()).isEqualTo(DEFAULT_TO_DATE);

    }

    @Test
    @Transactional
    public void createLocationWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = locationRepository.findAll().size();

        // Create the Location with an existing ID
        Location existingLocation = new Location();
        existingLocation.setId(1L);
        LocationDTO existingLocationDTO = locationMapper.locationToLocationDTO(existingLocation);

        // An entity with an existing ID cannot be created, so this API call must fail
        restLocationMockMvc.perform(post("/api/locations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingLocationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkGeoPointIsRequired() throws Exception {
        int databaseSizeBeforeTest = locationRepository.findAll().size();
        // set the field null
        location.geoPoint(null);

        // Create the Location, which fails.
        LocationDTO locationDTO = locationMapper.locationToLocationDTO(location);

        restLocationMockMvc.perform(post("/api/locations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(locationDTO)))
            .andExpect(status().isBadRequest());

        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkFromDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = locationRepository.findAll().size();
        // set the field null
        location.setFromDate(null);

        // Create the Location, which fails.
        LocationDTO locationDTO = locationMapper.locationToLocationDTO(location);

        restLocationMockMvc.perform(post("/api/locations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(locationDTO)))
            .andExpect(status().isBadRequest());

        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkToDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = locationRepository.findAll().size();
        // set the field null
        location.setToDate(null);

        // Create the Location, which fails.
        LocationDTO locationDTO = locationMapper.locationToLocationDTO(location);

        restLocationMockMvc.perform(post("/api/locations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(locationDTO)))
            .andExpect(status().isBadRequest());

        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllLocations() throws Exception {
        // Initialize the database
        locationRepository.saveAndFlush(location);

        // Get all the locationList
        restLocationMockMvc.perform(get("/api/locations?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(location.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].address").value(DEFAULT_ADDRESS.toString()))
            .andExpect(jsonPath("$.[*].searchName").value(DEFAULT_SEARCH_NAME.toString()))
            .andExpect(jsonPath("$.[*].geoPoint.lat").value(hasItem(DEFAULT_LAT.doubleValue())))
            .andExpect(jsonPath("$.[*].geoPoint.lon").value(hasItem(DEFAULT_LON.doubleValue())))
            .andExpect(jsonPath("$.[*].fromDate").value(hasItem(sameInstant(DEFAULT_FROM_DATE))))
            .andExpect(jsonPath("$.[*].toDate").value(hasItem(sameInstant(DEFAULT_TO_DATE))));
    }

    @Test
    @Transactional
    public void getLocation() throws Exception {
        // Initialize the database
        locationRepository.saveAndFlush(location);

        // Get the location
        restLocationMockMvc.perform(get("/api/locations/{id}", location.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(location.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.geoPoint.lat").value(DEFAULT_LAT.doubleValue()))
            .andExpect(jsonPath("$.geoPoint.lon").value(DEFAULT_LON.doubleValue()))
            .andExpect(jsonPath("$.fromDate").value(sameInstant(DEFAULT_FROM_DATE)))
            .andExpect(jsonPath("$.toDate").value(sameInstant(DEFAULT_TO_DATE)));
    }

    @Test
    @Transactional
    public void getNonExistingLocation() throws Exception {
        // Get the location
        restLocationMockMvc.perform(get("/api/locations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateLocation() throws Exception {
        // Initialize the database
        locationRepository.saveAndFlush(location);
        locationSearchRepository.save(location);
        int databaseSizeBeforeUpdate = locationRepository.findAll().size();

        // Update the location
        Location updatedLocation = locationRepository.findOne(location.getId());
        updatedLocation
                .name(UPDATED_NAME)
                .description(UPDATED_DESCRIPTION)
                .address(UPDATED_ADDRESS)
                .searchName(UPDATED_SEARCH_NAME)
                .geoPoint(new GeoPoint(UPDATED_LAT, UPDATED_LON))
                .fromDate(UPDATED_FROM_DATE)
                .toDate(UPDATED_TO_DATE);
        LocationDTO locationDTO = locationMapper.locationToLocationDTO(updatedLocation);

        restLocationMockMvc.perform(put("/api/locations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(locationDTO)))
            .andExpect(status().isOk());

        // Validate the Location in the database
        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeUpdate);
        Location testLocation = locationList.get(locationList.size() - 1);
        assertThat(testLocation.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testLocation.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testLocation.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testLocation.getSearchName()).isEqualTo(UPDATED_SEARCH_NAME);
        assertThat(testLocation.getGeoPoint().getLat()).isEqualTo(UPDATED_LAT);
        assertThat(testLocation.getGeoPoint().getLon()).isEqualTo(UPDATED_LON);
        assertThat(testLocation.getFromDate()).isEqualTo(UPDATED_FROM_DATE);
        assertThat(testLocation.getToDate()).isEqualTo(UPDATED_TO_DATE);

        // Validate the Location in ElasticSearch
        Location locationEs = locationSearchRepository.findOne(testLocation.getId());
        assertThat(locationEs.getName()).isEqualTo(UPDATED_NAME);
        assertThat(locationEs.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(locationEs.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(locationEs.getSearchName()).isEqualTo(UPDATED_SEARCH_NAME);
        assertThat(locationEs.getGeoPoint().getLat()).isEqualTo(UPDATED_LAT);
        assertThat(locationEs.getGeoPoint().getLon()).isEqualTo(UPDATED_LON);
        assertThat(locationEs.getFromDate()).isEqualTo(UPDATED_FROM_DATE);
        assertThat(locationEs.getToDate()).isEqualTo(UPDATED_TO_DATE);

    }

    @Test
    @Transactional
    public void updateNonExistingLocation() throws Exception {
        int databaseSizeBeforeUpdate = locationRepository.findAll().size();

        // Create the Location
        LocationDTO locationDTO = locationMapper.locationToLocationDTO(location);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restLocationMockMvc.perform(put("/api/locations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(locationDTO)))
            .andExpect(status().isCreated());

        // Validate the Location in the database
        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteLocation() throws Exception {
        // Initialize the database
        locationRepository.saveAndFlush(location);
        locationSearchRepository.save(location);
        int databaseSizeBeforeDelete = locationRepository.findAll().size();

        // Get the location
        restLocationMockMvc.perform(delete("/api/locations/{id}", location.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean locationExistsInEs = locationSearchRepository.exists(location.getId());
        assertThat(locationExistsInEs).isFalse();

        // Validate the database is empty
        List<Location> locationList = locationRepository.findAll();
        assertThat(locationList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchLocation() throws Exception {
        // Initialize the database
        locationRepository.saveAndFlush(location);
        locationSearchRepository.save(location);

        // Search the location
        restLocationMockMvc.perform(get("/api/_search/locations?query=id:" + location.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(location.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS.toString())))
            .andExpect(jsonPath("$.[*].searchName").value(hasItem(DEFAULT_SEARCH_NAME.toString())))
            .andExpect(jsonPath("$.[*].geoPoint.lat").value(hasItem(DEFAULT_LAT.doubleValue())))
            .andExpect(jsonPath("$.[*].geoPoint.lon").value(hasItem(DEFAULT_LON.doubleValue())))
            .andExpect(jsonPath("$.[*].fromDate").value(hasItem(sameInstant(DEFAULT_FROM_DATE))))
            .andExpect(jsonPath("$.[*].toDate").value(hasItem(sameInstant(DEFAULT_TO_DATE))));
    }

    @Test
    @Transactional
    public void searchLocationNearby() throws Exception {
         // Initialize the database
        locationRepository.saveAndFlush(location);
        locationSearchRepository.save(location);

        String locationString = "/api/_search/locations-nearby?lat=-90&lon=-180&distance=1km";
        restLocationMockMvc.perform(get(locationString))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(location.getId().intValue())));

        String locationStringOutside = "/api/_search/locations-nearby?lat=-80&lon=-170&distance=1km";
        restLocationMockMvc.perform(get(locationStringOutside))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*]").isEmpty());
    }
}
