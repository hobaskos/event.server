package io.hobaskos.event.web.rest;

import io.hobaskos.event.BackendApp;

import io.hobaskos.event.domain.EventCategory;
import io.hobaskos.event.repository.EventCategoryRepository;
import io.hobaskos.event.repository.search.EventCategorySearchRepository;
import io.hobaskos.event.service.dto.EventCategoryDTO;
import io.hobaskos.event.service.mapper.EventCategoryMapper;

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
import org.springframework.util.Base64Utils;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.hobaskos.event.domain.enumeration.EventCategoryTheme;
/**
 * Test class for the EventCategoryResource REST controller.
 *
 * @see EventCategoryResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApp.class)
public class EventCategoryResourceIntTest {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final byte[] DEFAULT_ICON = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_ICON = TestUtil.createByteArray(5000000, "1");
    private static final String DEFAULT_ICON_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_ICON_CONTENT_TYPE = "image/png";

    private static final EventCategoryTheme DEFAULT_THEME = EventCategoryTheme.RED;
    private static final EventCategoryTheme UPDATED_THEME = EventCategoryTheme.ORANGE;

    @Inject
    private EventCategoryRepository eventCategoryRepository;

    @Inject
    private EventCategoryMapper eventCategoryMapper;

    @Inject
    private EventCategorySearchRepository eventCategorySearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restEventCategoryMockMvc;

    private EventCategory eventCategory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EventCategoryResource eventCategoryResource = new EventCategoryResource();
        ReflectionTestUtils.setField(eventCategoryResource, "eventCategorySearchRepository", eventCategorySearchRepository);
        ReflectionTestUtils.setField(eventCategoryResource, "eventCategoryRepository", eventCategoryRepository);
        ReflectionTestUtils.setField(eventCategoryResource, "eventCategoryMapper", eventCategoryMapper);
        this.restEventCategoryMockMvc = MockMvcBuilders.standaloneSetup(eventCategoryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventCategory createEntity(EntityManager em) {
        EventCategory eventCategory = new EventCategory()
                .title(DEFAULT_TITLE)
                .icon(DEFAULT_ICON)
                .iconContentType(DEFAULT_ICON_CONTENT_TYPE)
                .theme(DEFAULT_THEME);
        return eventCategory;
    }

    @Before
    public void initTest() {
        eventCategorySearchRepository.deleteAll();
        eventCategory = createEntity(em);
    }

    @Test
    @Transactional
    public void createEventCategory() throws Exception {
        int databaseSizeBeforeCreate = eventCategoryRepository.findAll().size();

        // Create the EventCategory
        EventCategoryDTO eventCategoryDTO = eventCategoryMapper.eventCategoryToEventCategoryDTO(eventCategory);

        restEventCategoryMockMvc.perform(post("/api/event-categories")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventCategoryDTO)))
            .andExpect(status().isCreated());

        // Validate the EventCategory in the database
        List<EventCategory> eventCategoryList = eventCategoryRepository.findAll();
        assertThat(eventCategoryList).hasSize(databaseSizeBeforeCreate + 1);
        EventCategory testEventCategory = eventCategoryList.get(eventCategoryList.size() - 1);
        assertThat(testEventCategory.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testEventCategory.getIcon()).isEqualTo(DEFAULT_ICON);
        assertThat(testEventCategory.getIconContentType()).isEqualTo(DEFAULT_ICON_CONTENT_TYPE);
        assertThat(testEventCategory.getTheme()).isEqualTo(DEFAULT_THEME);

        // Validate the EventCategory in ElasticSearch
        EventCategory eventCategoryEs = eventCategorySearchRepository.findOne(testEventCategory.getId());
        assertThat(eventCategoryEs).isEqualToComparingFieldByField(testEventCategory);
    }

    @Test
    @Transactional
    public void createEventCategoryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = eventCategoryRepository.findAll().size();

        // Create the EventCategory with an existing ID
        EventCategory existingEventCategory = new EventCategory();
        existingEventCategory.setId(1L);
        EventCategoryDTO existingEventCategoryDTO = eventCategoryMapper.eventCategoryToEventCategoryDTO(existingEventCategory);

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventCategoryMockMvc.perform(post("/api/event-categories")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingEventCategoryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<EventCategory> eventCategoryList = eventCategoryRepository.findAll();
        assertThat(eventCategoryList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventCategoryRepository.findAll().size();
        // set the field null
        eventCategory.setTitle(null);

        // Create the EventCategory, which fails.
        EventCategoryDTO eventCategoryDTO = eventCategoryMapper.eventCategoryToEventCategoryDTO(eventCategory);

        restEventCategoryMockMvc.perform(post("/api/event-categories")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventCategoryDTO)))
            .andExpect(status().isBadRequest());

        List<EventCategory> eventCategoryList = eventCategoryRepository.findAll();
        assertThat(eventCategoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkIconIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventCategoryRepository.findAll().size();
        // set the field null
        eventCategory.setIcon(null);

        // Create the EventCategory, which fails.
        EventCategoryDTO eventCategoryDTO = eventCategoryMapper.eventCategoryToEventCategoryDTO(eventCategory);

        restEventCategoryMockMvc.perform(post("/api/event-categories")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventCategoryDTO)))
            .andExpect(status().isBadRequest());

        List<EventCategory> eventCategoryList = eventCategoryRepository.findAll();
        assertThat(eventCategoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkThemeIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventCategoryRepository.findAll().size();
        // set the field null
        eventCategory.setTheme(null);

        // Create the EventCategory, which fails.
        EventCategoryDTO eventCategoryDTO = eventCategoryMapper.eventCategoryToEventCategoryDTO(eventCategory);

        restEventCategoryMockMvc.perform(post("/api/event-categories")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventCategoryDTO)))
            .andExpect(status().isBadRequest());

        List<EventCategory> eventCategoryList = eventCategoryRepository.findAll();
        assertThat(eventCategoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllEventCategories() throws Exception {
        // Initialize the database
        eventCategoryRepository.saveAndFlush(eventCategory);

        // Get all the eventCategoryList
        restEventCategoryMockMvc.perform(get("/api/event-categories?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventCategory.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].iconContentType").value(hasItem(DEFAULT_ICON_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].icon").value(hasItem(Base64Utils.encodeToString(DEFAULT_ICON))))
            .andExpect(jsonPath("$.[*].theme").value(hasItem(DEFAULT_THEME.toString())));
    }

    @Test
    @Transactional
    public void getEventCategory() throws Exception {
        // Initialize the database
        eventCategoryRepository.saveAndFlush(eventCategory);

        // Get the eventCategory
        restEventCategoryMockMvc.perform(get("/api/event-categories/{id}", eventCategory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(eventCategory.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.iconContentType").value(DEFAULT_ICON_CONTENT_TYPE))
            .andExpect(jsonPath("$.icon").value(Base64Utils.encodeToString(DEFAULT_ICON)))
            .andExpect(jsonPath("$.theme").value(DEFAULT_THEME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingEventCategory() throws Exception {
        // Get the eventCategory
        restEventCategoryMockMvc.perform(get("/api/event-categories/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEventCategory() throws Exception {
        // Initialize the database
        eventCategoryRepository.saveAndFlush(eventCategory);
        eventCategorySearchRepository.save(eventCategory);
        int databaseSizeBeforeUpdate = eventCategoryRepository.findAll().size();

        // Update the eventCategory
        EventCategory updatedEventCategory = eventCategoryRepository.findOne(eventCategory.getId());
        updatedEventCategory
                .title(UPDATED_TITLE)
                .icon(UPDATED_ICON)
                .iconContentType(UPDATED_ICON_CONTENT_TYPE)
                .theme(UPDATED_THEME);
        EventCategoryDTO eventCategoryDTO = eventCategoryMapper.eventCategoryToEventCategoryDTO(updatedEventCategory);

        restEventCategoryMockMvc.perform(put("/api/event-categories")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventCategoryDTO)))
            .andExpect(status().isOk());

        // Validate the EventCategory in the database
        List<EventCategory> eventCategoryList = eventCategoryRepository.findAll();
        assertThat(eventCategoryList).hasSize(databaseSizeBeforeUpdate);
        EventCategory testEventCategory = eventCategoryList.get(eventCategoryList.size() - 1);
        assertThat(testEventCategory.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testEventCategory.getIcon()).isEqualTo(UPDATED_ICON);
        assertThat(testEventCategory.getIconContentType()).isEqualTo(UPDATED_ICON_CONTENT_TYPE);
        assertThat(testEventCategory.getTheme()).isEqualTo(UPDATED_THEME);

        // Validate the EventCategory in ElasticSearch
        EventCategory eventCategoryEs = eventCategorySearchRepository.findOne(testEventCategory.getId());
        assertThat(eventCategoryEs).isEqualToComparingFieldByField(testEventCategory);
    }

    @Test
    @Transactional
    public void updateNonExistingEventCategory() throws Exception {
        int databaseSizeBeforeUpdate = eventCategoryRepository.findAll().size();

        // Create the EventCategory
        EventCategoryDTO eventCategoryDTO = eventCategoryMapper.eventCategoryToEventCategoryDTO(eventCategory);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restEventCategoryMockMvc.perform(put("/api/event-categories")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventCategoryDTO)))
            .andExpect(status().isCreated());

        // Validate the EventCategory in the database
        List<EventCategory> eventCategoryList = eventCategoryRepository.findAll();
        assertThat(eventCategoryList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteEventCategory() throws Exception {
        // Initialize the database
        eventCategoryRepository.saveAndFlush(eventCategory);
        eventCategorySearchRepository.save(eventCategory);
        int databaseSizeBeforeDelete = eventCategoryRepository.findAll().size();

        // Get the eventCategory
        restEventCategoryMockMvc.perform(delete("/api/event-categories/{id}", eventCategory.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean eventCategoryExistsInEs = eventCategorySearchRepository.exists(eventCategory.getId());
        assertThat(eventCategoryExistsInEs).isFalse();

        // Validate the database is empty
        List<EventCategory> eventCategoryList = eventCategoryRepository.findAll();
        assertThat(eventCategoryList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchEventCategory() throws Exception {
        // Initialize the database
        eventCategoryRepository.saveAndFlush(eventCategory);
        eventCategorySearchRepository.save(eventCategory);

        // Search the eventCategory
        restEventCategoryMockMvc.perform(get("/api/_search/event-categories?query=id:" + eventCategory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventCategory.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].iconContentType").value(hasItem(DEFAULT_ICON_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].icon").value(hasItem(Base64Utils.encodeToString(DEFAULT_ICON))))
            .andExpect(jsonPath("$.[*].theme").value(hasItem(DEFAULT_THEME.toString())));
    }
}
