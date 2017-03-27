package io.hobaskos.event.web.rest;

import io.hobaskos.event.BackendApp;
import io.hobaskos.event.domain.Authority;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.domain.enumeration.DeviceType;
import io.hobaskos.event.repository.AuthorityRepository;
import io.hobaskos.event.repository.UserRepository;
import io.hobaskos.event.security.AuthoritiesConstants;
import io.hobaskos.event.service.DeviceService;
import io.hobaskos.event.service.MailService;
import io.hobaskos.event.service.UserConnectionService;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.dto.DeviceDTO;
import io.hobaskos.event.service.dto.UserDTO;
import io.hobaskos.event.web.rest.vm.ManagedUserVM;
import org.junit.Before;
import org.junit.Ignore;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the AccountResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApp.class)
public class AccountResourceIntTest {

    @Inject
    private UserRepository userRepository;

    @Inject
    private AuthorityRepository authorityRepository;

    @Inject
    private UserService userService;

    @Inject
    private UserConnectionService userConnectionService;

    @Mock
    private UserService mockUserService;

    @Mock
    private MailService mockMailService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private DeviceService deviceService;

    @Inject
    private EntityManager em;

    private MockMvc restUserMockMvc;

    private MockMvc restMvc;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mockMailService).sendActivationEmail((User) anyObject());

        AccountResource accountResource = new AccountResource();
        ReflectionTestUtils.setField(accountResource, "userRepository", userRepository);
        ReflectionTestUtils.setField(accountResource, "userService", userService);
        ReflectionTestUtils.setField(accountResource, "mailService", mockMailService);

        ReflectionTestUtils.setField(deviceService, "userService", mockUserService);

        AccountResource accountUserMockResource = new AccountResource();
        ReflectionTestUtils.setField(accountUserMockResource, "userRepository", userRepository);
        ReflectionTestUtils.setField(accountUserMockResource, "userService", mockUserService);
        ReflectionTestUtils.setField(accountUserMockResource, "deviceService", deviceService);
        ReflectionTestUtils.setField(accountUserMockResource, "userConnectionService", userConnectionService);
        ReflectionTestUtils.setField(accountUserMockResource, "mailService", mockMailService);

        this.restMvc = MockMvcBuilders.standaloneSetup(accountResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    public User generateJohnDoeUser() {
        Set<Authority> authorities = new HashSet<>();
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.ADMIN);
        authorities.add(authority);

        User user = new User();
        user.setLogin("test");
        user.setFirstName("john");
        user.setLastName("doe");
        user.setEmail("john.doe@jhipter.com");
        user.setAuthorities(authorities);
        return user;
    }

    @Test
    public void testNonAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/authenticate")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void testAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/authenticate")
                .with(request -> {
                    request.setRemoteUser("test");
                    return request;
                })
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("\"test\""));
    }

    @Test
    public void testGetExistingAccount() throws Exception {
        User user = generateJohnDoeUser();
        when(mockUserService.getUserWithAuthorities()).thenReturn(user);

        restUserMockMvc.perform(get("/api/account")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.login").value("test"))
                .andExpect(jsonPath("$.firstName").value("john"))
                .andExpect(jsonPath("$.lastName").value("doe"))
                .andExpect(jsonPath("$.email").value("john.doe@jhipter.com"))
                .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN));
    }

    @Test
    public void testGetUnknownAccount() throws Exception {
        when(mockUserService.getUserWithAuthorities()).thenReturn(null);

        restUserMockMvc.perform(get("/api/account")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    public void testRegisterValid() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM(
            null,                   // id
            "joe",                  // login
            "password",             // password
            "Joe",                  // firstName
            "Shmoe",                // lastName
            "joe@example.com",      // e-mail
            "http://localhost:8080/files/someFile.png",
            true,                   // activated
            "en",                   // langKey
            new HashSet<>(Arrays.asList(AuthoritiesConstants.USER)),
            null,                   // createdBy
            null,                   // createdDate
            null,                   // lastModifiedBy
            null                    // lastModifiedDate
        );

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<User> user = userRepository.findOneByLogin("joe");
        assertThat(user.isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testRegisterInvalidLogin() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM(
            null,                   // id
            "funky-log!n",          // login <-- invalid
            "password",             // password
            "Funky",                // firstName
            "One",                  // lastName
            "funky@example.com",    // e-mail
            "http://localhost:8080/files/someFile.png",
            true,                   // activated
            "en",                   // langKey
            new HashSet<>(Arrays.asList(AuthoritiesConstants.USER)),
            null,                   // createdBy
            null,                   // createdDate
            null,                   // lastModifiedBy
            null                    // lastModifiedDate
        );

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByEmail("funky@example.com");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterInvalidEmail() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM(
            null,               // id
            "bob",              // login
            "password",         // password
            "Bob",              // firstName
            "Green",            // lastName
            "invalid",          // e-mail <-- invalid
            "http://localhost:8080/files/someFile.png",
            true,               // activated
            "en",               // langKey
            new HashSet<>(Arrays.asList(AuthoritiesConstants.USER)),
            null,               // createdBy
            null,               // createdDate
            null,               // lastModifiedBy
            null                // lastModifiedDate
        );

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByLogin("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterInvalidPassword() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM(
            null,               // id
            "bob",              // login
            "123",              // password with only 3 digits
            "Bob",              // firstName
            "Green",            // lastName
            "bob@example.com",  // e-mail
            "http://localhost:8080/files/someFile.png",
            true,               // activated
            "en",               // langKey
            new HashSet<>(Arrays.asList(AuthoritiesConstants.USER)),
            null,               // createdBy
            null,               // createdDate
            null,               // lastModifiedBy
            null                // lastModifiedDate
        );

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByLogin("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterDuplicateLogin() throws Exception {
        // Good
        ManagedUserVM validUser = new ManagedUserVM(
            null,                   // id
            "alice",                // login
            "password",             // password
            "Alice",                // firstName
            "Something",            // lastName
            "alice@example.com",    // e-mail
            "http://localhost:8080/files/someFile.png",
            true,                   // activated
            "en",                   // langKey
            new HashSet<>(Arrays.asList(AuthoritiesConstants.USER)),
            null,                   // createdBy
            null,                   // createdDate
            null,                   // lastModifiedBy
            null                    // lastModifiedDate
        );

        // Duplicate login, different e-mail
        ManagedUserVM duplicatedUser = new ManagedUserVM(validUser.getId(), validUser.getLogin(), validUser.getPassword(), validUser.getLogin(), validUser.getLastName(),
            "alicejr@example.com", validUser.getProfileImageUrl(), true, validUser.getLangKey(), validUser.getAuthorities(), validUser.getCreatedBy(), validUser.getCreatedDate(), validUser.getLastModifiedBy(), validUser.getLastModifiedDate());

        // Good user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        // Duplicate login
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(duplicatedUser)))
            .andExpect(status().is4xxClientError());

        Optional<User> userDup = userRepository.findOneByEmail("alicejr@example.com");
        assertThat(userDup.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterDuplicateEmail() throws Exception {
        // Good
        ManagedUserVM validUser = new ManagedUserVM(
            null,                   // id
            "john",                 // login
            "password",             // password
            "John",                 // firstName
            "Doe",                  // lastName
            "john@example.com",     // e-mail
            "http://localhost:8080/files/someFile.png",
            true,                   // activated
            "en",                   // langKey
            new HashSet<>(Arrays.asList(AuthoritiesConstants.USER)),
            null,                   // createdBy
            null,                   // createdDate
            null,                   // lastModifiedBy
            null                    // lastModifiedDate
        );

        // Duplicate e-mail, different login
        ManagedUserVM duplicatedUser = new ManagedUserVM(validUser.getId(), "johnjr", validUser.getPassword(), validUser.getLogin(), validUser.getLastName(),
            validUser.getEmail(), validUser.getProfileImageUrl(),true, validUser.getLangKey(), validUser.getAuthorities(), validUser.getCreatedBy(), validUser.getCreatedDate(), validUser.getLastModifiedBy(), validUser.getLastModifiedDate());

        // Good user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        // Duplicate e-mail
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(duplicatedUser)))
            .andExpect(status().is4xxClientError());

        Optional<User> userDup = userRepository.findOneByLogin("johnjr");
        assertThat(userDup.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterAdminIsIgnored() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM(
            null,                   // id
            "badguy",               // login
            "password",             // password
            "Bad",                  // firstName
            "Guy",                  // lastName
            "badguy@example.com",   // e-mail
            "http://localhost:8080/files/someFile.png",
            true,                   // activated
            "en",                   // langKey
            new HashSet<>(Arrays.asList(AuthoritiesConstants.ADMIN)),
            null,                   // createdBy
            null,                   // createdDate
            null,                   // lastModifiedBy
            null                    // lastModifiedDate
        );

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<User> userDup = userRepository.findOneByLogin("badguy");
        assertThat(userDup.isPresent()).isTrue();
        assertThat(userDup.get().getAuthorities()).hasSize(1)
            .containsExactly(authorityRepository.findOne(AuthoritiesConstants.USER));
    }

    @Test
    @Transactional
    public void testSaveInvalidLogin() throws Exception {
        UserDTO invalidUser = new UserDTO(
            "funky-log!n",          // login <-- invalid
            "Funky",                // firstName
            "One",                  // lastName
            "funky@example.com",    // e-mail
            "http://localhost:8080/files/someFile.png",
            true,                   // activated
            "en",                   // langKey
            new HashSet<>(Arrays.asList(AuthoritiesConstants.USER))
        );

        restUserMockMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByEmail("funky@example.com");
        assertThat(user.isPresent()).isFalse();
    }

   @Test
    public void testEmptyFollowersForUser() throws Exception {
        User user = generateJohnDoeUser();
        when(mockUserService.getUserWithAuthorities()).thenReturn(user);

        restMvc.perform(get("/api/account/followers")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }

    @Test
    public void testEmptyFollowingForUser() throws Exception {
        User user = generateJohnDoeUser();
        when(mockUserService.getUserWithAuthorities()).thenReturn(user);

        restMvc.perform(get("/api/account/following")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }

    @Test
    public void testEmptyUserConnectionsForUser() throws Exception {
        User user = generateJohnDoeUser();
        when(mockUserService.getUserWithAuthorities()).thenReturn(user);

        restMvc.perform(get("/api/account/user-connections?type=PENDING")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        restMvc.perform(get("/api/account/user-connections?type=CONFIRMED")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }

    @Test
    @Transactional
    public void testFollower() throws Exception {
        User randomUser1 = UserResourceIntTest.createRandomEntity(em);
        User randomUser2 = UserResourceIntTest.createRandomEntity(em);

        when(mockUserService.getUserWithAuthorities()).thenReturn(randomUser1);
        when(mockUserService.getUserWithAuthoritiesByLogin(anyString())).thenReturn(Optional.of(randomUser2));
        restUserMockMvc.perform(post("/api/account/following/" + randomUser2.getLogin())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.login").value(randomUser2.getLogin()));
    }

    @Test
    @Transactional
    public void testRequestForUserConnection() throws Exception {
        User randomUser1 = UserResourceIntTest.createRandomEntity(em);
        User randomUser2 = UserResourceIntTest.createRandomEntity(em);

        when(mockUserService.getUserWithAuthorities()).thenReturn(randomUser1);
        when(mockUserService.getUserWithAuthoritiesByLogin(anyString())).thenReturn(Optional.of(randomUser2));
        restUserMockMvc.perform(post("/api/account/user-connections/" + randomUser2.getLogin())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        restUserMockMvc.perform(post("/api/account/user-connections/" + randomUser2.getLogin())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        when(mockUserService.getUserWithAuthoritiesByLogin(anyString())).thenReturn(Optional.of(randomUser1));
        restUserMockMvc.perform(post("/api/account/user-connections/" + randomUser1.getLogin())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void addAndFetchDevices() throws Exception {
        String deviceToken = "TOKENTOKEN";
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setToken(deviceToken);
        deviceDTO.setType(DeviceType.IOS);

        User randomUser1 = UserResourceIntTest.createRandomEntity(em);

        when(mockUserService.getUserWithAuthorities()).thenReturn(randomUser1);
        restUserMockMvc.perform(post("/api/account/devices")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(deviceDTO)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.token").value(deviceToken))
            .andExpect(jsonPath("$.type").value(DeviceType.IOS.toString()))
            .andExpect(jsonPath("$.userLogin").value(randomUser1.getLogin()));
    }
}
