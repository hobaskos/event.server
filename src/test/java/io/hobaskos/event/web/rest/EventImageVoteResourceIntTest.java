package io.hobaskos.event.web.rest;

import io.hobaskos.event.BackendApp;

import io.hobaskos.event.domain.EventImageVote;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.domain.EventImage;
import io.hobaskos.event.repository.EventImageVoteRepository;
import io.hobaskos.event.repository.search.EventImageVoteSearchRepository;
import io.hobaskos.event.service.EventImageService;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.UserServiceIntTest;
import io.hobaskos.event.service.dto.EventImageVoteDTO;
import io.hobaskos.event.service.mapper.EventImageVoteMapper;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Test class for the EventImageVoteResource REST controller.
 *
 * @see EventImageVoteResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApp.class)
public class EventImageVoteResourceIntTest {

    private static final Integer DEFAULT_VOTE = -1;
    private static final Integer UPDATED_VOTE = 0;

    @Inject
    private EventImageVoteRepository eventImageVoteRepository;

    @Inject
    private EventImageVoteMapper eventImageVoteMapper;

    @Inject
    private EventImageVoteSearchRepository eventImageVoteSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EventImageService eventImageService;

    @Inject
    private EntityManager em;

    @Mock
    private UserService userService;

    private MockMvc restEventImageVoteMockMvc;

    private EventImageVote eventImageVote;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EventImageVoteResource eventImageVoteResource = new EventImageVoteResource();
        ReflectionTestUtils.setField(eventImageVoteResource, "eventImageVoteSearchRepository", eventImageVoteSearchRepository);
        ReflectionTestUtils.setField(eventImageVoteResource, "eventImageVoteRepository", eventImageVoteRepository);
        ReflectionTestUtils.setField(eventImageVoteResource, "eventImageVoteMapper", eventImageVoteMapper);
        ReflectionTestUtils.setField(eventImageVoteResource, "userService", userService);
        ReflectionTestUtils.setField(eventImageVoteResource, "eventImageService", eventImageService);
        this.restEventImageVoteMockMvc = MockMvcBuilders.standaloneSetup(eventImageVoteResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventImageVote createEntity(EntityManager em) {
        EventImageVote eventImageVote = new EventImageVote()
                .vote(DEFAULT_VOTE);
        // Add required entity
        User user = UserResourceIntTest.createEntity(em);
        em.persist(user);
        em.flush();
        eventImageVote.setUser(user);
        // Add required entity
        EventImage eventImage = EventImageResourceIntTest.createEntity(em);
        em.persist(eventImage);
        em.flush();
        eventImageVote.setEventImage(eventImage);
        return eventImageVote;
    }

    @Before
    public void initTest() {
        eventImageVoteSearchRepository.deleteAll();
        eventImageVote = createEntity(em);
    }

    @Test
    @Transactional
    public void createEventImageVote() throws Exception {
        int databaseSizeBeforeCreate = eventImageVoteRepository.findAll().size();

        // Create the EventImageVote
        EventImageVoteDTO eventImageVoteDTO = eventImageVoteMapper.eventImageVoteToEventImageVoteDTO(eventImageVote);

        when(userService.getUserWithAuthorities()).thenReturn(UserResourceIntTest.createRandomEntity(em));
        restEventImageVoteMockMvc.perform(post("/api/event-image-votes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventImageVoteDTO)))
            .andExpect(status().isCreated());

        // Validate the EventImageVote in the database
        List<EventImageVote> eventImageVoteList = eventImageVoteRepository.findAll();
        assertThat(eventImageVoteList).hasSize(databaseSizeBeforeCreate + 1);
        EventImageVote testEventImageVote = eventImageVoteList.get(eventImageVoteList.size() - 1);
        assertThat(testEventImageVote.getVote()).isEqualTo(DEFAULT_VOTE);

        // Validate the EventImageVote in ElasticSearch
        //EventImageVote eventImageVoteEs = eventImageVoteSearchRepository.findOne(testEventImageVote.getId());
        //assertThat(eventImageVoteEs).isEqualToComparingFieldByField(testEventImageVote);
    }

    @Test
    @Transactional
    public void createEventImageVoteWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = eventImageVoteRepository.findAll().size();

        // Create the EventImageVote with an existing ID
        EventImageVote existingEventImageVote = new EventImageVote();
        existingEventImageVote.setId(1L);
        EventImageVoteDTO existingEventImageVoteDTO = eventImageVoteMapper.eventImageVoteToEventImageVoteDTO(existingEventImageVote);

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventImageVoteMockMvc.perform(post("/api/event-image-votes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingEventImageVoteDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<EventImageVote> eventImageVoteList = eventImageVoteRepository.findAll();
        assertThat(eventImageVoteList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkVoteIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventImageVoteRepository.findAll().size();
        // set the field null
        eventImageVote.setVote(null);

        // Create the EventImageVote, which fails.
        EventImageVoteDTO eventImageVoteDTO = eventImageVoteMapper.eventImageVoteToEventImageVoteDTO(eventImageVote);

        when(userService.getUserWithAuthorities()).thenReturn(UserResourceIntTest.createRandomEntity(em));
        restEventImageVoteMockMvc.perform(post("/api/event-image-votes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventImageVoteDTO)))
            .andExpect(status().isBadRequest());

        List<EventImageVote> eventImageVoteList = eventImageVoteRepository.findAll();
        assertThat(eventImageVoteList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllEventImageVotes() throws Exception {
        // Initialize the database
        eventImageVoteRepository.saveAndFlush(eventImageVote);

        // Get all the eventImageVoteList
        restEventImageVoteMockMvc.perform(get("/api/event-image-votes?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventImageVote.getId().intValue())))
            .andExpect(jsonPath("$.[*].vote").value(hasItem(DEFAULT_VOTE)));
    }

    @Test
    @Transactional
    public void getEventImageVote() throws Exception {
        // Initialize the database
        eventImageVoteRepository.saveAndFlush(eventImageVote);

        // Get the eventImageVote
        restEventImageVoteMockMvc.perform(get("/api/event-image-votes/{id}", eventImageVote.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(eventImageVote.getId().intValue()))
            .andExpect(jsonPath("$.vote").value(DEFAULT_VOTE));
    }

    @Test
    @Transactional
    public void getNonExistingEventImageVote() throws Exception {
        // Get the eventImageVote
        restEventImageVoteMockMvc.perform(get("/api/event-image-votes/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void deleteEventImageVote() throws Exception {
        // Initialize the database
        eventImageVoteRepository.saveAndFlush(eventImageVote);
        eventImageVoteSearchRepository.save(eventImageVote);
        int databaseSizeBeforeDelete = eventImageVoteRepository.findAll().size();

        // Get the eventImageVote
        restEventImageVoteMockMvc.perform(delete("/api/event-image-votes/{id}", eventImageVote.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean eventImageVoteExistsInEs = eventImageVoteSearchRepository.exists(eventImageVote.getId());
        assertThat(eventImageVoteExistsInEs).isFalse();

        // Validate the database is empty
        List<EventImageVote> eventImageVoteList = eventImageVoteRepository.findAll();
        assertThat(eventImageVoteList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchEventImageVote() throws Exception {
        // Initialize the database
        eventImageVoteRepository.saveAndFlush(eventImageVote);
        eventImageVoteSearchRepository.save(eventImageVote);

        // Search the eventImageVote
        restEventImageVoteMockMvc.perform(get("/api/_search/event-image-votes?query=id:" + eventImageVote.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventImageVote.getId().intValue())))
            .andExpect(jsonPath("$.[*].vote").value(hasItem(DEFAULT_VOTE)));
    }
}
