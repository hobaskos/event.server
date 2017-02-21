package io.hobaskos.event.web.rest;

import io.hobaskos.event.BackendApp;

import io.hobaskos.event.domain.UserConnection;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.repository.UserConnectionRepository;
import io.hobaskos.event.service.UserConnectionService;
import io.hobaskos.event.repository.search.UserConnectionSearchRepository;
import io.hobaskos.event.service.dto.UserConnectionDTO;
import io.hobaskos.event.service.mapper.UserConnectionMapper;

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

import io.hobaskos.event.domain.enumeration.UserConnectionType;
/**
 * Test class for the UserConnectionResource REST controller.
 *
 * @see UserConnectionResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApp.class)
public class UserConnectionResourceIntTest {

    private static final UserConnectionType DEFAULT_TYPE = UserConnectionType.FOLLOWER;
    private static final UserConnectionType UPDATED_TYPE = UserConnectionType.PENDING;

    @Inject
    private UserConnectionRepository userConnectionRepository;

    @Inject
    private UserConnectionMapper userConnectionMapper;

    @Inject
    private UserConnectionService userConnectionService;

    @Inject
    private UserConnectionSearchRepository userConnectionSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restUserConnectionMockMvc;

    private UserConnection userConnection;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        UserConnectionResource userConnectionResource = new UserConnectionResource();
        ReflectionTestUtils.setField(userConnectionResource, "userConnectionService", userConnectionService);
        this.restUserConnectionMockMvc = MockMvcBuilders.standaloneSetup(userConnectionResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserConnection createEntity(EntityManager em) {
        UserConnection userConnection = new UserConnection()
                .type(DEFAULT_TYPE);
        // Add required entity
        User requester = UserResourceIntTest.createEntity(em);
        em.persist(requester);
        em.flush();
        userConnection.setRequester(requester);
        // Add required entity
        User requestee = UserResourceIntTest.createEntity(em);
        em.persist(requestee);
        em.flush();
        userConnection.setRequestee(requestee);
        return userConnection;
    }

    @Before
    public void initTest() {
        userConnectionSearchRepository.deleteAll();
        userConnection = createEntity(em);
    }

    @Test
    @Transactional
    public void createUserConnection() throws Exception {
        int databaseSizeBeforeCreate = userConnectionRepository.findAll().size();

        // Create the UserConnection
        UserConnectionDTO userConnectionDTO = userConnectionMapper.userConnectionToUserConnectionDTO(userConnection);

        restUserConnectionMockMvc.perform(post("/api/user-connections")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userConnectionDTO)))
            .andExpect(status().isCreated());

        // Validate the UserConnection in the database
        List<UserConnection> userConnectionList = userConnectionRepository.findAll();
        assertThat(userConnectionList).hasSize(databaseSizeBeforeCreate + 1);
        UserConnection testUserConnection = userConnectionList.get(userConnectionList.size() - 1);
        assertThat(testUserConnection.getType()).isEqualTo(DEFAULT_TYPE);

        // Validate the UserConnection in ElasticSearch
        UserConnection userConnectionEs = userConnectionSearchRepository.findOne(testUserConnection.getId());
        assertThat(userConnectionEs).isEqualToComparingFieldByField(testUserConnection);
    }

    @Test
    @Transactional
    public void createUserConnectionWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = userConnectionRepository.findAll().size();

        // Create the UserConnection with an existing ID
        UserConnection existingUserConnection = new UserConnection();
        existingUserConnection.setId(1L);
        UserConnectionDTO existingUserConnectionDTO = userConnectionMapper.userConnectionToUserConnectionDTO(existingUserConnection);

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserConnectionMockMvc.perform(post("/api/user-connections")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingUserConnectionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<UserConnection> userConnectionList = userConnectionRepository.findAll();
        assertThat(userConnectionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = userConnectionRepository.findAll().size();
        // set the field null
        userConnection.setType(null);

        // Create the UserConnection, which fails.
        UserConnectionDTO userConnectionDTO = userConnectionMapper.userConnectionToUserConnectionDTO(userConnection);

        restUserConnectionMockMvc.perform(post("/api/user-connections")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userConnectionDTO)))
            .andExpect(status().isBadRequest());

        List<UserConnection> userConnectionList = userConnectionRepository.findAll();
        assertThat(userConnectionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllUserConnections() throws Exception {
        // Initialize the database
        userConnectionRepository.saveAndFlush(userConnection);

        // Get all the userConnectionList
        restUserConnectionMockMvc.perform(get("/api/user-connections?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userConnection.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }

    @Test
    @Transactional
    public void getUserConnection() throws Exception {
        // Initialize the database
        userConnectionRepository.saveAndFlush(userConnection);

        // Get the userConnection
        restUserConnectionMockMvc.perform(get("/api/user-connections/{id}", userConnection.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(userConnection.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingUserConnection() throws Exception {
        // Get the userConnection
        restUserConnectionMockMvc.perform(get("/api/user-connections/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateUserConnection() throws Exception {
        // Initialize the database
        userConnectionRepository.saveAndFlush(userConnection);
        userConnectionSearchRepository.save(userConnection);
        int databaseSizeBeforeUpdate = userConnectionRepository.findAll().size();

        // Update the userConnection
        UserConnection updatedUserConnection = userConnectionRepository.findOne(userConnection.getId());
        updatedUserConnection
                .type(UPDATED_TYPE);
        UserConnectionDTO userConnectionDTO = userConnectionMapper.userConnectionToUserConnectionDTO(updatedUserConnection);

        restUserConnectionMockMvc.perform(put("/api/user-connections")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userConnectionDTO)))
            .andExpect(status().isOk());

        // Validate the UserConnection in the database
        List<UserConnection> userConnectionList = userConnectionRepository.findAll();
        assertThat(userConnectionList).hasSize(databaseSizeBeforeUpdate);
        UserConnection testUserConnection = userConnectionList.get(userConnectionList.size() - 1);
        assertThat(testUserConnection.getType()).isEqualTo(UPDATED_TYPE);

        // Validate the UserConnection in ElasticSearch
        UserConnection userConnectionEs = userConnectionSearchRepository.findOne(testUserConnection.getId());
        assertThat(userConnectionEs).isEqualToComparingFieldByField(testUserConnection);
    }

    @Test
    @Transactional
    public void updateNonExistingUserConnection() throws Exception {
        int databaseSizeBeforeUpdate = userConnectionRepository.findAll().size();

        // Create the UserConnection
        UserConnectionDTO userConnectionDTO = userConnectionMapper.userConnectionToUserConnectionDTO(userConnection);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restUserConnectionMockMvc.perform(put("/api/user-connections")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userConnectionDTO)))
            .andExpect(status().isCreated());

        // Validate the UserConnection in the database
        List<UserConnection> userConnectionList = userConnectionRepository.findAll();
        assertThat(userConnectionList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteUserConnection() throws Exception {
        // Initialize the database
        userConnectionRepository.saveAndFlush(userConnection);
        userConnectionSearchRepository.save(userConnection);
        int databaseSizeBeforeDelete = userConnectionRepository.findAll().size();

        // Get the userConnection
        restUserConnectionMockMvc.perform(delete("/api/user-connections/{id}", userConnection.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean userConnectionExistsInEs = userConnectionSearchRepository.exists(userConnection.getId());
        assertThat(userConnectionExistsInEs).isFalse();

        // Validate the database is empty
        List<UserConnection> userConnectionList = userConnectionRepository.findAll();
        assertThat(userConnectionList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchUserConnection() throws Exception {
        // Initialize the database
        userConnectionRepository.saveAndFlush(userConnection);
        userConnectionSearchRepository.save(userConnection);

        // Search the userConnection
        restUserConnectionMockMvc.perform(get("/api/_search/user-connections?query=id:" + userConnection.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userConnection.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }
}
