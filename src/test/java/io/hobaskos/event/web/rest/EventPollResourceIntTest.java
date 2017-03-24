package io.hobaskos.event.web.rest;

import io.hobaskos.event.BackendApp;

import io.hobaskos.event.domain.EventPoll;
import io.hobaskos.event.domain.Event;
import io.hobaskos.event.repository.EventPollRepository;
import io.hobaskos.event.service.EventPollService;
import io.hobaskos.event.repository.search.EventPollSearchRepository;
import io.hobaskos.event.service.dto.EventPollDTO;
import io.hobaskos.event.service.mapper.EventPollMapper;

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

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.hobaskos.event.domain.enumeration.EventPollStatus;
/**
 * Test class for the EventPollResource REST controller.
 *
 * @see EventPollResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApp.class)
public class EventPollResourceIntTest {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final EventPollStatus DEFAULT_STATUS = EventPollStatus.INACTIVE;
    private static final EventPollStatus UPDATED_STATUS = EventPollStatus.NOMINATION;

    @Inject
    private EventPollRepository eventPollRepository;

    @Inject
    private EventPollMapper eventPollMapper;

    @Inject
    private EventPollService eventPollService;

    @Inject
    private EventPollSearchRepository eventPollSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restEventPollMockMvc;

    private EventPoll eventPoll;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EventPollResource eventPollResource = new EventPollResource();
        ReflectionTestUtils.setField(eventPollResource, "eventPollService", eventPollService);
        this.restEventPollMockMvc = MockMvcBuilders.standaloneSetup(eventPollResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventPoll createEntity(EntityManager em) {
        EventPoll eventPoll = new EventPoll()
                .title(DEFAULT_TITLE)
                .description(DEFAULT_DESCRIPTION)
                .status(DEFAULT_STATUS);
        // Add required entity
        Event event = EventResourceIntTest.createEntity(em);
        em.persist(event);
        em.flush();
        eventPoll.setEvent(event);
        return eventPoll;
    }

    @Before
    public void initTest() {
        eventPollSearchRepository.deleteAll();
        eventPoll = createEntity(em);
    }

    @Test
    @Transactional
    public void createEventPoll() throws Exception {
        int databaseSizeBeforeCreate = eventPollRepository.findAll().size();

        // Create the EventPoll
        EventPollDTO eventPollDTO = eventPollMapper.eventPollToEventPollDTO(eventPoll);

        restEventPollMockMvc.perform(post("/api/event-polls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventPollDTO)))
            .andExpect(status().isCreated());

        // Validate the EventPoll in the database
        List<EventPoll> eventPollList = eventPollRepository.findAll();
        assertThat(eventPollList).hasSize(databaseSizeBeforeCreate + 1);
        EventPoll testEventPoll = eventPollList.get(eventPollList.size() - 1);
        assertThat(testEventPoll.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testEventPoll.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testEventPoll.getStatus()).isEqualTo(DEFAULT_STATUS);

        // Validate the EventPoll in ElasticSearch
        //EventPoll eventPollEs = eventPollSearchRepository.findOne(testEventPoll.getId());
        //assertThat(eventPollEs).isEqualToComparingFieldByField(testEventPoll);
    }

    @Test
    @Transactional
    public void createEventPollWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = eventPollRepository.findAll().size();

        // Create the EventPoll with an existing ID
        EventPoll existingEventPoll = new EventPoll();
        existingEventPoll.setId(1L);
        EventPollDTO existingEventPollDTO = eventPollMapper.eventPollToEventPollDTO(existingEventPoll);

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventPollMockMvc.perform(post("/api/event-polls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingEventPollDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<EventPoll> eventPollList = eventPollRepository.findAll();
        assertThat(eventPollList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventPollRepository.findAll().size();
        // set the field null
        eventPoll.setTitle(null);

        // Create the EventPoll, which fails.
        EventPollDTO eventPollDTO = eventPollMapper.eventPollToEventPollDTO(eventPoll);

        restEventPollMockMvc.perform(post("/api/event-polls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventPollDTO)))
            .andExpect(status().isBadRequest());

        List<EventPoll> eventPollList = eventPollRepository.findAll();
        assertThat(eventPollList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventPollRepository.findAll().size();
        // set the field null
        eventPoll.setStatus(null);

        // Create the EventPoll, which fails.
        EventPollDTO eventPollDTO = eventPollMapper.eventPollToEventPollDTO(eventPoll);

        restEventPollMockMvc.perform(post("/api/event-polls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventPollDTO)))
            .andExpect(status().isBadRequest());

        List<EventPoll> eventPollList = eventPollRepository.findAll();
        assertThat(eventPollList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllEventPolls() throws Exception {
        // Initialize the database
        eventPollRepository.saveAndFlush(eventPoll);

        // Get all the eventPollList
        restEventPollMockMvc.perform(get("/api/event-polls?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventPoll.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @Test
    @Transactional
    public void getEventPoll() throws Exception {
        // Initialize the database
        eventPollRepository.saveAndFlush(eventPoll);

        // Get the eventPoll
        restEventPollMockMvc.perform(get("/api/event-polls/{id}", eventPoll.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(eventPoll.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingEventPoll() throws Exception {
        // Get the eventPoll
        restEventPollMockMvc.perform(get("/api/event-polls/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEventPoll() throws Exception {
        // Initialize the database
        eventPollRepository.saveAndFlush(eventPoll);
        eventPollSearchRepository.save(eventPoll);
        int databaseSizeBeforeUpdate = eventPollRepository.findAll().size();

        // Update the eventPoll
        EventPoll updatedEventPoll = eventPollRepository.findOne(eventPoll.getId());
        updatedEventPoll
                .title(UPDATED_TITLE)
                .description(UPDATED_DESCRIPTION)
                .status(UPDATED_STATUS);
        EventPollDTO eventPollDTO = eventPollMapper.eventPollToEventPollDTO(updatedEventPoll);

        restEventPollMockMvc.perform(put("/api/event-polls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventPollDTO)))
            .andExpect(status().isOk());

        // Validate the EventPoll in the database
        List<EventPoll> eventPollList = eventPollRepository.findAll();
        assertThat(eventPollList).hasSize(databaseSizeBeforeUpdate);
        EventPoll testEventPoll = eventPollList.get(eventPollList.size() - 1);
        assertThat(testEventPoll.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testEventPoll.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testEventPoll.getStatus()).isEqualTo(UPDATED_STATUS);

        // Validate the EventPoll in ElasticSearch
        //EventPoll eventPollEs = eventPollSearchRepository.findOne(testEventPoll.getId());
        //assertThat(eventPollEs).isEqualToComparingFieldByField(testEventPoll);
    }

    @Test
    @Transactional
    public void updateNonExistingEventPoll() throws Exception {
        int databaseSizeBeforeUpdate = eventPollRepository.findAll().size();

        // Create the EventPoll
        EventPollDTO eventPollDTO = eventPollMapper.eventPollToEventPollDTO(eventPoll);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restEventPollMockMvc.perform(put("/api/event-polls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventPollDTO)))
            .andExpect(status().isCreated());

        // Validate the EventPoll in the database
        List<EventPoll> eventPollList = eventPollRepository.findAll();
        assertThat(eventPollList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteEventPoll() throws Exception {
        // Initialize the database
        eventPollRepository.saveAndFlush(eventPoll);
        eventPollSearchRepository.save(eventPoll);
        int databaseSizeBeforeDelete = eventPollRepository.findAll().size();

        // Get the eventPoll
        restEventPollMockMvc.perform(delete("/api/event-polls/{id}", eventPoll.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean eventPollExistsInEs = eventPollSearchRepository.exists(eventPoll.getId());
        assertThat(eventPollExistsInEs).isFalse();

        // Validate the database is empty
        List<EventPoll> eventPollList = eventPollRepository.findAll();
        assertThat(eventPollList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchEventPoll() throws Exception {
        // Initialize the database
        eventPollRepository.saveAndFlush(eventPoll);
        eventPollSearchRepository.save(eventPoll);

        // Search the eventPoll
        restEventPollMockMvc.perform(get("/api/_search/event-polls?query=id:" + eventPoll.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventPoll.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }
}
