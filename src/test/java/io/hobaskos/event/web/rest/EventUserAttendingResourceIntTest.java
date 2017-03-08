package io.hobaskos.event.web.rest;

import io.hobaskos.event.BackendApp;

import io.hobaskos.event.domain.EventUserAttending;
import io.hobaskos.event.domain.Event;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.repository.EventUserAttendingRepository;
import io.hobaskos.event.repository.search.EventUserAttendingSearchRepository;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.dto.EventUserAttendingDTO;
import io.hobaskos.event.service.mapper.EventUserAttendingMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
import static org.mockito.Mockito.*;

import io.hobaskos.event.domain.enumeration.EventAttendingType;
/**
 * Test class for the EventUserAttendingResource REST controller.
 *
 * @see EventUserAttendingResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApp.class)
public class EventUserAttendingResourceIntTest {

    private static final ZonedDateTime DEFAULT_CREATED_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final EventAttendingType DEFAULT_TYPE = EventAttendingType.GOING;
    private static final EventAttendingType UPDATED_TYPE = EventAttendingType.THINKING;

    @Inject
    private EventUserAttendingRepository eventUserAttendingRepository;

    @Inject
    private EventUserAttendingMapper eventUserAttendingMapper;

    @Inject
    private EventUserAttendingSearchRepository eventUserAttendingSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    @Mock
    private UserService userService;

    private MockMvc restEventUserAttendingMockMvc;

    private EventUserAttending eventUserAttending;

    private User user;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EventUserAttendingResource eventUserAttendingResource = new EventUserAttendingResource();
        ReflectionTestUtils.setField(eventUserAttendingResource, "eventUserAttendingSearchRepository", eventUserAttendingSearchRepository);
        ReflectionTestUtils.setField(eventUserAttendingResource, "eventUserAttendingRepository", eventUserAttendingRepository);
        ReflectionTestUtils.setField(eventUserAttendingResource, "eventUserAttendingMapper", eventUserAttendingMapper);
        ReflectionTestUtils.setField(eventUserAttendingResource, "userService", userService);
        this.restEventUserAttendingMockMvc = MockMvcBuilders.standaloneSetup(eventUserAttendingResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void setupUser() {
        user = UserResourceIntTest.createEntity(em);
        em.persist(user);
        em.flush();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventUserAttending createEntity(EntityManager em) {
        EventUserAttending eventUserAttending = new EventUserAttending()
                .createdDate(DEFAULT_CREATED_DATE)
                .type(DEFAULT_TYPE);
        // Add required entity
        Event event = EventResourceIntTest.createEntity(em);
        em.persist(event);
        em.flush();
        eventUserAttending.setEvent(event);
        // Add required entity
        User user = UserResourceIntTest.createEntity(em);
        em.persist(user);
        em.flush();
        eventUserAttending.setUser(user);
        return eventUserAttending;
    }

    @Before
    public void initTest() {
        eventUserAttendingSearchRepository.deleteAll();
        eventUserAttending = createEntity(em);
    }

    @Test
    @Transactional
    public void createEventUserAttending() throws Exception {
        int databaseSizeBeforeCreate = eventUserAttendingRepository.findAll().size();

        // Create the EventUserAttending
        eventUserAttending.setUser(null);
        EventUserAttendingDTO eventUserAttendingDTO = eventUserAttendingMapper.eventUserAttendingToEventUserAttendingDTO(eventUserAttending);

        when(userService.getUserWithAuthorities()).thenReturn(user);
        restEventUserAttendingMockMvc.perform(post("/api/event-user-attendings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventUserAttendingDTO)))
            .andExpect(status().isCreated());

        // Validate the EventUserAttending in the database
        List<EventUserAttending> eventUserAttendingList = eventUserAttendingRepository.findAll();
        assertThat(eventUserAttendingList).hasSize(databaseSizeBeforeCreate + 1);
        EventUserAttending testEventUserAttending = eventUserAttendingList.get(eventUserAttendingList.size() - 1);
        assertThat(testEventUserAttending.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testEventUserAttending.getUser()).isEqualTo(user);
    }

    @Test
    @Transactional
    public void createEventUserAttendingTwoTimes() throws Exception {

        // Create the EventUserAttending
        EventUserAttendingDTO firstEventUserAttendingDTO = new EventUserAttendingDTO();
        firstEventUserAttendingDTO.setEventId(eventUserAttending.getEvent().getId());
        firstEventUserAttendingDTO.setType(EventAttendingType.GOING);
        EventUserAttendingDTO secondEventUserAttendingDTO = new EventUserAttendingDTO();
        secondEventUserAttendingDTO.setEventId(eventUserAttending.getEvent().getId());
        secondEventUserAttendingDTO.setType(EventAttendingType.GOING);

        when(userService.getUserWithAuthorities()).thenReturn(user);
        restEventUserAttendingMockMvc.perform(post("/api/event-user-attendings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(firstEventUserAttendingDTO)))
            .andExpect(status().isCreated());

        when(userService.getUserWithAuthorities()).thenReturn(user);
        restEventUserAttendingMockMvc.perform(post("/api/event-user-attendings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(secondEventUserAttendingDTO)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void createEventUserAttendingWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = eventUserAttendingRepository.findAll().size();

        // Create the EventUserAttending with an existing ID
        EventUserAttending existingEventUserAttending = new EventUserAttending();
        existingEventUserAttending.setId(1L);
        EventUserAttendingDTO existingEventUserAttendingDTO = eventUserAttendingMapper.eventUserAttendingToEventUserAttendingDTO(existingEventUserAttending);

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventUserAttendingMockMvc.perform(post("/api/event-user-attendings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingEventUserAttendingDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<EventUserAttending> eventUserAttendingList = eventUserAttendingRepository.findAll();
        assertThat(eventUserAttendingList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventUserAttendingRepository.findAll().size();
        // set the field null
        eventUserAttending.setType(null);

        // Create the EventUserAttending, which fails.
        EventUserAttendingDTO eventUserAttendingDTO = eventUserAttendingMapper.eventUserAttendingToEventUserAttendingDTO(eventUserAttending);

        restEventUserAttendingMockMvc.perform(post("/api/event-user-attendings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventUserAttendingDTO)))
            .andExpect(status().isBadRequest());

        List<EventUserAttending> eventUserAttendingList = eventUserAttendingRepository.findAll();
        assertThat(eventUserAttendingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllEventUserAttendings() throws Exception {
        // Initialize the database
        eventUserAttendingRepository.saveAndFlush(eventUserAttending);

        // Get all the eventUserAttendingList
        restEventUserAttendingMockMvc.perform(get("/api/event-user-attendings?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventUserAttending.getId().intValue())))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(sameInstant(DEFAULT_CREATED_DATE))))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }

    @Test
    @Transactional
    public void getEventUserAttending() throws Exception {
        // Initialize the database
        eventUserAttendingRepository.saveAndFlush(eventUserAttending);

        // Get the eventUserAttending
        restEventUserAttendingMockMvc.perform(get("/api/event-user-attendings/{id}", eventUserAttending.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(eventUserAttending.getId().intValue()))
            .andExpect(jsonPath("$.createdDate").value(sameInstant(DEFAULT_CREATED_DATE)))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingEventUserAttending() throws Exception {
        // Get the eventUserAttending
        restEventUserAttendingMockMvc.perform(get("/api/event-user-attendings/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEventUserAttending() throws Exception {
        // Initialize the database
        eventUserAttendingRepository.saveAndFlush(eventUserAttending);
        eventUserAttendingSearchRepository.save(eventUserAttending);
        int databaseSizeBeforeUpdate = eventUserAttendingRepository.findAll().size();

        // Update the eventUserAttending
        EventUserAttending updatedEventUserAttending = eventUserAttendingRepository.findOne(eventUserAttending.getId());
        updatedEventUserAttending
                .createdDate(UPDATED_CREATED_DATE)
                .type(UPDATED_TYPE);
        updatedEventUserAttending.setUser(null);
        EventUserAttendingDTO eventUserAttendingDTO = eventUserAttendingMapper.eventUserAttendingToEventUserAttendingDTO(updatedEventUserAttending);

        when(userService.getUserWithAuthorities()).thenReturn(user);
        restEventUserAttendingMockMvc.perform(put("/api/event-user-attendings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventUserAttendingDTO)))
            .andExpect(status().isOk());

        // Validate the EventUserAttending in the database
        List<EventUserAttending> eventUserAttendingList = eventUserAttendingRepository.findAll();
        assertThat(eventUserAttendingList).hasSize(databaseSizeBeforeUpdate);
        EventUserAttending testEventUserAttending = eventUserAttendingList.get(eventUserAttendingList.size() - 1);
        assertThat(testEventUserAttending.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testEventUserAttending.getType()).isEqualTo(UPDATED_TYPE);

        // Validate the EventUserAttending in ElasticSearch
        EventUserAttending eventUserAttendingEs = eventUserAttendingSearchRepository.findOne(testEventUserAttending.getId());
        assertThat(eventUserAttendingEs).isEqualToComparingFieldByField(testEventUserAttending);
    }

    @Test
    @Transactional
    public void updateNonExistingEventUserAttending() throws Exception {
        int databaseSizeBeforeUpdate = eventUserAttendingRepository.findAll().size();

        // Create the EventUserAttending
        eventUserAttending.setUser(null);
        EventUserAttendingDTO eventUserAttendingDTO = eventUserAttendingMapper.eventUserAttendingToEventUserAttendingDTO(eventUserAttending);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        when(userService.getUserWithAuthorities()).thenReturn(user);
        restEventUserAttendingMockMvc.perform(put("/api/event-user-attendings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventUserAttendingDTO)))
            .andExpect(status().isCreated());

        // Validate the EventUserAttending in the database
        List<EventUserAttending> eventUserAttendingList = eventUserAttendingRepository.findAll();
        assertThat(eventUserAttendingList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteEventUserAttending() throws Exception {
        // Initialize the database
        eventUserAttendingRepository.saveAndFlush(eventUserAttending);
        eventUserAttendingSearchRepository.save(eventUserAttending);
        int databaseSizeBeforeDelete = eventUserAttendingRepository.findAll().size();

        // Get the eventUserAttending
        restEventUserAttendingMockMvc.perform(delete("/api/event-user-attendings/{id}", eventUserAttending.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean eventUserAttendingExistsInEs = eventUserAttendingSearchRepository.exists(eventUserAttending.getId());
        assertThat(eventUserAttendingExistsInEs).isFalse();

        // Validate the database is empty
        List<EventUserAttending> eventUserAttendingList = eventUserAttendingRepository.findAll();
        assertThat(eventUserAttendingList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchEventUserAttending() throws Exception {
        // Initialize the database
        eventUserAttendingRepository.saveAndFlush(eventUserAttending);
        eventUserAttendingSearchRepository.save(eventUserAttending);

        // Search the eventUserAttending
        restEventUserAttendingMockMvc.perform(get("/api/_search/event-user-attendings?query=id:" + eventUserAttending.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventUserAttending.getId().intValue())))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(sameInstant(DEFAULT_CREATED_DATE))))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }
}
