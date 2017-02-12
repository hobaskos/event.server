package io.hobaskos.event.web.rest;

import io.hobaskos.event.BackendApp;

import io.hobaskos.event.domain.Event;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.repository.EventRepository;
import io.hobaskos.event.repository.UserRepository;
import io.hobaskos.event.repository.search.LocationSearchRepository;
import io.hobaskos.event.service.EventService;
import io.hobaskos.event.repository.search.EventSearchRepository;
import io.hobaskos.event.service.LocationService;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.dto.EventDTO;
import io.hobaskos.event.service.dto.LocationDTO;
import io.hobaskos.event.service.mapper.EventMapper;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Test class for the EventResource REST controller.
 *
 * @see EventResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApp.class)
public class EventResourceIntTest {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_IMAGE_URL = "AAAAAAAAAA";
    private static final String UPDATED_IMAGE_URL = "BBBBBBBBBB";

    private static final String DEFAULT_LOCATION_NAME = "CCCCCCCC";
    private static final String DEFAULT_LOCATION_DESCRIPTION = "DDDDDDDDD";
    private static final Double DEFAULT_LOCATION_LAT = 12.0000000D;
    private static final Double DEFAULT_LOCATION_LON = 13.0000000D;
    private static final Double UPDATED_LOCATION_LAT = 13.0000000D;
    private static final Double UPDATED_LOCATION_LON = 14.0000000D;
    private static final int DEFAULT_LOCATION_FROM_DATE_DAYS = 1;
    private static final int DEFAULT_LOCATION_TO_DATE_DAYS = 2;
    private static final int UPDATED_LOCATION_FROM_DATE_DAYS = 30;
    private static final int UPDATED_LOCATION_TO_DATE_DAYS = 31;

    @Inject
    private UserRepository userRepository;

    @Inject
    private EventRepository eventRepository;

    @Inject
    private EventMapper eventMapper;

    @Inject
    private EventService eventService;

    @Inject
    private LocationService locationService;

    @Inject
    private EventSearchRepository eventSearchRepository;

    @Inject
    private LocationSearchRepository locationSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Mock
    private UserService mockUserService;

    @Inject
    private EntityManager em;

    private MockMvc restEventMockMvc;

    private Event event;

    private User owner;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EventResource eventResource = new EventResource();
        ReflectionTestUtils.setField(eventResource, "eventService", eventService);
        ReflectionTestUtils.setField(eventService, "userService", mockUserService);

        this.restEventMockMvc = MockMvcBuilders.standaloneSetup(eventResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();

        owner = UserResourceIntTest.createOwnerEntity(em);
        when(mockUserService.getUserWithAuthorities()).thenReturn(owner);
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Event createEntity(EntityManager em) {
        Event event = new Event()
                .title(DEFAULT_TITLE)
                .description(DEFAULT_DESCRIPTION)
                .imageUrl(DEFAULT_IMAGE_URL);
        // Add required entity
        User owner = UserResourceIntTest.createEntity(em);
        em.persist(owner);
        em.flush();
        event.setOwner(owner);
        return event;
    }

    @Before
    public void initTest() {
        eventSearchRepository.deleteAll();
        locationSearchRepository.deleteAll();
        event = createEntity(em);
    }

    @Test
    @Transactional
    public void createEvent() throws Exception {
        int databaseSizeBeforeCreate = eventRepository.findAll().size();

        // Create the Event
        EventDTO eventDTO = eventMapper.eventToEventDTO(event);

        restEventMockMvc.perform(post("/api/events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventDTO)))
            .andExpect(status().isCreated());

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeCreate + 1);
        Event testEvent = eventList.get(eventList.size() - 1);
        assertThat(testEvent.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testEvent.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testEvent.getImageUrl()).isEqualTo(DEFAULT_IMAGE_URL);

        // Validate the Event in ElasticSearch
        Event eventEs = eventSearchRepository.findOne(testEvent.getId());
        assertThat(eventEs).isEqualToComparingFieldByField(testEvent);
    }

    @Test
    @Transactional
    public void createEventWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = eventRepository.findAll().size();

        // Create the Event with an existing ID
        Event existingEvent = new Event();
        existingEvent.setId(1L);
        EventDTO existingEventDTO = eventMapper.eventToEventDTO(existingEvent);

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventMockMvc.perform(post("/api/events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingEventDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllEvents() throws Exception {
        // Initialize the database
        eventRepository.saveAndFlush(event);

        // Get all the eventList
        restEventMockMvc.perform(get("/api/events?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(event.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].imageUrl").value(hasItem(DEFAULT_IMAGE_URL.toString())));
    }

    @Test
    @Transactional
    public void getEvent() throws Exception {
        // Initialize the database
        eventRepository.saveAndFlush(event);

        // Get the event
        restEventMockMvc.perform(get("/api/events/{id}", event.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(event.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.imageUrl").value(DEFAULT_IMAGE_URL.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingEvent() throws Exception {
        // Get the event
        restEventMockMvc.perform(get("/api/events/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEvent() throws Exception {
        // Initialize the database
        eventRepository.saveAndFlush(event);
        eventSearchRepository.save(event);
        int databaseSizeBeforeUpdate = eventRepository.findAll().size();

        // Update the event
        Event updatedEvent = eventRepository.findOne(event.getId());
        updatedEvent
                .title(UPDATED_TITLE)
                .description(UPDATED_DESCRIPTION)
                .imageUrl(UPDATED_IMAGE_URL);
        EventDTO eventDTO = eventMapper.eventToEventDTO(updatedEvent);

        restEventMockMvc.perform(put("/api/events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventDTO)))
            .andExpect(status().isOk());

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeUpdate);
        Event testEvent = eventList.get(eventList.size() - 1);
        assertThat(testEvent.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testEvent.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testEvent.getImageUrl()).isEqualTo(UPDATED_IMAGE_URL);

        // Validate the Event in ElasticSearch
        Event eventEs = eventSearchRepository.findOne(testEvent.getId());
        assertThat(eventEs).isEqualToComparingFieldByField(testEvent);
    }

    @Test
    @Transactional
    public void updateNonExistingEvent() throws Exception {
        int databaseSizeBeforeUpdate = eventRepository.findAll().size();

        // Create the Event
        EventDTO eventDTO = eventMapper.eventToEventDTO(event);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restEventMockMvc.perform(put("/api/events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventDTO)))
            .andExpect(status().isCreated());

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteEvent() throws Exception {
        // Initialize the database
        eventRepository.saveAndFlush(event);
        eventSearchRepository.save(event);
        int databaseSizeBeforeDelete = eventRepository.findAll().size();

        // Get the event
        restEventMockMvc.perform(delete("/api/events/{id}", event.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean eventExistsInEs = eventSearchRepository.exists(event.getId());
        assertThat(eventExistsInEs).isFalse();

        // Validate the database is empty
        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchEvent() throws Exception {
        // Initialize the database
        eventRepository.saveAndFlush(event);
        eventSearchRepository.save(event);

        // Search the event
        restEventMockMvc.perform(get("/api/_search/events?query=id:" + event.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(event.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].imageUrl").value(hasItem(DEFAULT_IMAGE_URL.toString())));
    }

    @Test
    @Transactional
    public void searchEventFullText() throws Exception {
        // Initialize the database
        eventRepository.saveAndFlush(event);
        eventSearchRepository.save(event);

        // Search for events using the title
        restEventMockMvc.perform(get("/api/_search/events?query=" + event.getTitle()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)));

        // Search the event using the description
        restEventMockMvc.perform(get("/api/_search/events?query=" + event.getDescription()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));

        // Search the event using the image url
        restEventMockMvc.perform(get("/api/_search/events?query=" + event.getImageUrl()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].imageUrl").value(hasItem(DEFAULT_IMAGE_URL)));

        // Search for event using and expect no data
        restEventMockMvc.perform(get("/api/_search/events?query=" + UPDATED_DESCRIPTION))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*]").isEmpty());
    }

    @Test
    @Transactional
    public void attachLocationAndSearch() throws Exception {
        // Initialize the database
        eventRepository.saveAndFlush(event);
        eventSearchRepository.save(event);

        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setName(DEFAULT_LOCATION_NAME);
        locationDTO.setDescription(DEFAULT_LOCATION_DESCRIPTION);
        locationDTO.setVector(1);
        locationDTO.setGeoPoint(new GeoPoint(DEFAULT_LOCATION_LAT, DEFAULT_LOCATION_LON));
        locationDTO.setEventId(event.getId());
        locationDTO.setFromDate(ZonedDateTime.now().plusDays(DEFAULT_LOCATION_FROM_DATE_DAYS));
        locationDTO.setToDate(ZonedDateTime.now().plusDays(DEFAULT_LOCATION_TO_DATE_DAYS));
        locationDTO = locationService.save(locationDTO);

        // verify that the location is attached to the event
        restEventMockMvc.perform(get("/api/_search/events?query=" + event.getTitle()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].locations.[*].name").value(hasItem(DEFAULT_LOCATION_NAME)))
            .andExpect(jsonPath("$.[*].locations.[*].description").value(hasItem(DEFAULT_LOCATION_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].locations.[*].geoPoint.lat").value(hasItem(DEFAULT_LOCATION_LAT)))
            .andExpect(jsonPath("$.[*].locations.[*].geoPoint.lon").value(hasItem(DEFAULT_LOCATION_LON)));

        // Search the event using a nearby search on the location
        restEventMockMvc.perform(get(String.format("/api/_search/events-nearby?lat=%f&lon=%f&distance=100m",
                DEFAULT_LOCATION_LAT, DEFAULT_LOCATION_LON)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].locations.[*].name").value(hasItem(DEFAULT_LOCATION_NAME)))
            .andExpect(jsonPath("$.[*].locations.[*].description").value(hasItem(DEFAULT_LOCATION_DESCRIPTION)));

        // Search the event using a nearby search on a wrong location - expect no data.
        restEventMockMvc.perform(get(String.format("/api/_search/events-nearby?lat=%f&lon=%f&distance=100m",
                DEFAULT_LOCATION_LAT - 1, DEFAULT_LOCATION_LON - 1)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*]").isEmpty());

        // Move the location and check again
        locationDTO.setGeoPoint(new GeoPoint(UPDATED_LOCATION_LAT, UPDATED_LOCATION_LON));
        locationDTO = locationService.save(locationDTO);

         // Search the event with updated location data.
        restEventMockMvc.perform(get(String.format("/api/_search/events-nearby?lat=%f&lon=%f&distance=100m",
                UPDATED_LOCATION_LAT, UPDATED_LOCATION_LON)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].locations.[*].name").value(hasItem(DEFAULT_LOCATION_NAME)))
            .andExpect(jsonPath("$.[*].locations.[*].description").value(hasItem(DEFAULT_LOCATION_DESCRIPTION)));

        // Search the event using the old location data - expect no data.
        restEventMockMvc.perform(get(String.format("/api/_search/events-nearby?lat=%f&lon=%f&distance=100m",
                DEFAULT_LOCATION_LAT , DEFAULT_LOCATION_LON)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*]").isEmpty());

        // Set date outside the default range
        locationDTO.setFromDate(ZonedDateTime.now().plusDays(UPDATED_LOCATION_FROM_DATE_DAYS));
        locationDTO.setToDate(ZonedDateTime.now().plusDays(UPDATED_LOCATION_TO_DATE_DAYS));
        locationService.save(locationDTO);

        // Search the event with updated date data - expect nothing
        restEventMockMvc.perform(get(String.format("/api/_search/events-nearby?lat=%f&lon=%f&distance=100m",
                UPDATED_LOCATION_LAT, UPDATED_LOCATION_LON)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*]").isEmpty());

        // Search for the event with date params
        restEventMockMvc.perform(get(String.format("/api/_search/events-nearby?lat=%f&lon=%f&distance=100m&fromDate=%s&toDate=%s",
                UPDATED_LOCATION_LAT,
                UPDATED_LOCATION_LON,
                LocalDateTime.now().plusDays(UPDATED_LOCATION_FROM_DATE_DAYS - 1).toString() + "Z",
                LocalDateTime.now().plusDays(UPDATED_LOCATION_TO_DATE_DAYS + 1).toString() + "Z")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].locations.[*].name").value(hasItem(DEFAULT_LOCATION_NAME)))
            .andExpect(jsonPath("$.[*].locations.[*].description").value(hasItem(DEFAULT_LOCATION_DESCRIPTION)));
   }
}
