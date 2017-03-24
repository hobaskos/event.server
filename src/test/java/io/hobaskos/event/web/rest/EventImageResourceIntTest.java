package io.hobaskos.event.web.rest;

import io.hobaskos.event.BackendApp;

import io.hobaskos.event.domain.EventImage;
import io.hobaskos.event.domain.EventPoll;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.repository.EventImageRepository;
import io.hobaskos.event.service.EventImageService;
import io.hobaskos.event.repository.search.EventImageSearchRepository;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.dto.EventImageDTO;
import io.hobaskos.event.service.mapper.EventImageMapper;

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
import org.springframework.util.Base64Utils;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Test class for the EventImageResource REST controller.
 *
 * @see EventImageResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApp.class)
public class EventImageResourceIntTest {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_IMAGE_URL = "http://localhost:8080/files/someFile.png";
    private static final byte[] DEFAULT_FILE = TestUtil.createByteArray(1, "0");
    private static final String DEFAULT_FILE_CONTENT_TYPE = "image/jpg";

    @Inject
    private EventImageRepository eventImageRepository;

    @Inject
    private EventImageMapper eventImageMapper;

    @Inject
    private EventImageService eventImageService;

    @Inject
    private EventImageSearchRepository eventImageSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    @Mock
    private UserService userService;

    private MockMvc restEventImageMockMvc;

    private EventImage eventImage;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EventImageResource eventImageResource = new EventImageResource();
        ReflectionTestUtils.setField(eventImageService, "userService", userService);
        ReflectionTestUtils.setField(eventImageResource, "eventImageService", eventImageService);
        this.restEventImageMockMvc = MockMvcBuilders.standaloneSetup(eventImageResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventImage createEntity(EntityManager em) {
        EventImage eventImage = new EventImage()
                .title(DEFAULT_TITLE)
                .imageUrl(DEFAULT_IMAGE_URL);
        // Add required entity
        EventPoll poll = EventPollResourceIntTest.createEntity(em);
        em.persist(poll);
        em.flush();
        eventImage.setPoll(poll);
        // Add required entity
        User user = UserResourceIntTest.createEntity(em);
        em.persist(user);
        em.flush();
        eventImage.setUser(user);
        return eventImage;
    }

    @Before
    public void initTest() {
        eventImageSearchRepository.deleteAll();
        eventImage = createEntity(em);
    }

    @Test
    @Transactional
    public void createEventImage() throws Exception {
        int databaseSizeBeforeCreate = eventImageRepository.findAll().size();

        // Create the EventImage
        EventImageDTO eventImageDTO = eventImageMapper.eventImageToEventImageDTO(eventImage);
        eventImageDTO.setFile(DEFAULT_FILE);
        eventImageDTO.setFileContentType(DEFAULT_FILE_CONTENT_TYPE);

        when(userService.getUserWithAuthorities()).thenReturn(UserResourceIntTest.createRandomEntity(em));
        restEventImageMockMvc.perform(post("/api/event-images")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventImageDTO)))
            .andExpect(status().isCreated());

        // Validate the EventImage in the database
        List<EventImage> eventImageList = eventImageRepository.findAll();
        assertThat(eventImageList).hasSize(databaseSizeBeforeCreate + 1);
        EventImage testEventImage = eventImageList.get(eventImageList.size() - 1);
        assertThat(testEventImage.getTitle()).isEqualTo(DEFAULT_TITLE);

        // Validate the EventImage in ElasticSearch
        EventImage eventImageEs = eventImageSearchRepository.findOne(testEventImage.getId());
        assertThat(eventImageEs).isEqualToComparingFieldByField(testEventImage);
    }

    @Test
    @Transactional
    public void createEventImageWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = eventImageRepository.findAll().size();

        // Create the EventImage with an existing ID
        EventImage existingEventImage = new EventImage();
        existingEventImage.setId(1L);
        EventImageDTO existingEventImageDTO = eventImageMapper.eventImageToEventImageDTO(existingEventImage);

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventImageMockMvc.perform(post("/api/event-images")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingEventImageDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<EventImage> eventImageList = eventImageRepository.findAll();
        assertThat(eventImageList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllEventImages() throws Exception {
        // Initialize the database
        eventImageRepository.saveAndFlush(eventImage);

        // Get all the eventImageList
        restEventImageMockMvc.perform(get("/api/event-images?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventImage.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())));
    }

    @Test
    @Transactional
    public void getEventImage() throws Exception {
        // Initialize the database
        eventImageRepository.saveAndFlush(eventImage);

        // Get the eventImage
        restEventImageMockMvc.perform(get("/api/event-images/{id}", eventImage.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(eventImage.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingEventImage() throws Exception {
        // Get the eventImage
        restEventImageMockMvc.perform(get("/api/event-images/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEventImage() throws Exception {
        // Initialize the database
        eventImageRepository.saveAndFlush(eventImage);
        eventImageSearchRepository.save(eventImage);
        int databaseSizeBeforeUpdate = eventImageRepository.findAll().size();

        // Update the eventImage
        EventImage updatedEventImage = eventImageRepository.findOne(eventImage.getId());
        updatedEventImage.title(UPDATED_TITLE);
        EventImageDTO eventImageDTO = eventImageMapper.eventImageToEventImageDTO(updatedEventImage);
        eventImageDTO.setFile(DEFAULT_FILE);
        eventImageDTO.setFileContentType(DEFAULT_FILE_CONTENT_TYPE);

        when(userService.getUserWithAuthorities()).thenReturn(UserResourceIntTest.createRandomEntity(em));
        restEventImageMockMvc.perform(put("/api/event-images")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventImageDTO)))
            .andExpect(status().isOk());

        // Validate the EventImage in the database
        List<EventImage> eventImageList = eventImageRepository.findAll();
        assertThat(eventImageList).hasSize(databaseSizeBeforeUpdate);
        EventImage testEventImage = eventImageList.get(eventImageList.size() - 1);
        assertThat(testEventImage.getTitle()).isEqualTo(UPDATED_TITLE);

        // Validate the EventImage in ElasticSearch
        EventImage eventImageEs = eventImageSearchRepository.findOne(testEventImage.getId());
        assertThat(eventImageEs).isEqualToComparingFieldByField(testEventImage);
    }

    @Test
    @Transactional
    public void updateNonExistingEventImage() throws Exception {
        int databaseSizeBeforeUpdate = eventImageRepository.findAll().size();

        // Create the EventImage
        EventImageDTO eventImageDTO = eventImageMapper.eventImageToEventImageDTO(eventImage);
        eventImageDTO.setFile(DEFAULT_FILE);
        eventImageDTO.setFileContentType(DEFAULT_FILE_CONTENT_TYPE);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        when(userService.getUserWithAuthorities()).thenReturn(UserResourceIntTest.createRandomEntity(em));
        restEventImageMockMvc.perform(put("/api/event-images")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventImageDTO)))
            .andExpect(status().isCreated());

        // Validate the EventImage in the database
        List<EventImage> eventImageList = eventImageRepository.findAll();
        assertThat(eventImageList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteEventImage() throws Exception {
        // Initialize the database
        eventImageRepository.saveAndFlush(eventImage);
        eventImageSearchRepository.save(eventImage);
        int databaseSizeBeforeDelete = eventImageRepository.findAll().size();

        // Get the eventImage
        restEventImageMockMvc.perform(delete("/api/event-images/{id}", eventImage.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean eventImageExistsInEs = eventImageSearchRepository.exists(eventImage.getId());
        assertThat(eventImageExistsInEs).isFalse();

        // Validate the database is empty
        List<EventImage> eventImageList = eventImageRepository.findAll();
        assertThat(eventImageList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchEventImage() throws Exception {
        // Initialize the database
        eventImageRepository.saveAndFlush(eventImage);
        eventImageSearchRepository.save(eventImage);

        // Search the eventImage
        restEventImageMockMvc.perform(get("/api/_search/event-images?query=id:" + eventImage.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventImage.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())));
    }
}
