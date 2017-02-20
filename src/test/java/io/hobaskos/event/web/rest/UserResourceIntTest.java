package io.hobaskos.event.web.rest;

import io.hobaskos.event.BackendApp;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.repository.UserRepository;
import io.hobaskos.event.service.UserService;

import io.hobaskos.event.service.mapper.UserMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApp.class)
public class UserResourceIntTest {

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    private MockMvc restManagedUserMockMvc;
    private MockMvc restUserMockMvc;

    /**
     * Create a User.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which has a required relationship to the User entity.
     */
    public static User createEntity(EntityManager em) {
        User user = new User();
        user.setLogin("test");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail("test@test.com");
        user.setProfileImageUrl("http://localhost:8080/files/someFile.png");
        user.setFirstName("test");
        user.setLastName("test");
        user.setLangKey("en");
        em.persist(user);
        em.flush();
        return user;
    }

    /**
     * Create an Owner user
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which has a required relationship to the User entity.
     */
    public static User createOwnerEntity(EntityManager em) {
        User user = new User();
        user.setLogin("owner");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail("owner@test.com");
        user.setProfileImageUrl("http://localhost:8080/files/someOtherFile.png");
        user.setFirstName("owner");
        user.setLastName("owner");
        user.setLangKey("en");
        em.persist(user);
        em.flush();
        return user;
    }

    @Before
    public void setup() {
        ManagedUserResource managedUserResource = new ManagedUserResource();
        ReflectionTestUtils.setField(managedUserResource, "userRepository", userRepository);
        ReflectionTestUtils.setField(managedUserResource, "userService", userService);
        this.restManagedUserMockMvc = MockMvcBuilders.standaloneSetup(managedUserResource).build();

        UserResource userResource = new UserResource();
        ReflectionTestUtils.setField(userResource, "userRepository", userRepository);
        ReflectionTestUtils.setField(userResource, "userService", userService);
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource).build();
    }

    @Test
    public void testGetExistingUser() throws Exception {
        restManagedUserMockMvc.perform(get("/api/managed-users/admin")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.lastName").value("Administrator"));

        restUserMockMvc.perform(get("/api/users/admin")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.login").value("admin"));
    }

    @Test
    public void testGetUnknownUser() throws Exception {
        restManagedUserMockMvc.perform(get("/api/managed-users/unknown")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        restUserMockMvc.perform(get("/api/users/unknown")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetExistingUserWithAnEmailLogin() throws Exception {
        User user = userService.createUser("john.doe@localhost.com", "johndoe", "John", "Doe", "john.doe@localhost.com", "en-US");

        restManagedUserMockMvc.perform(get("/api/managed-users/john.doe@localhost.com")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.login").value("john.doe@localhost.com"));

        userRepository.delete(user);
    }

    @Test
    public void testDeleteExistingUserWithAnEmailLogin() throws Exception {
        User user = userService.createUser("john.doe@localhost.com", "johndoe", "John", "Doe", "john.doe@localhost.com", "en-US");

        restManagedUserMockMvc.perform(delete("/api/managed-users/john.doe@localhost.com")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(userRepository.findOneByLogin("john.doe@localhost.com").isPresent()).isFalse();

        userRepository.delete(user);
    }
}
