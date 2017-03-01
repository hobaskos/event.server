package io.hobaskos.event.service;

import io.hobaskos.event.domain.Authority;
import io.hobaskos.event.domain.SocialUserConnection;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.domain.enumeration.SocialType;
import io.hobaskos.event.repository.AuthorityRepository;
import io.hobaskos.event.repository.UserRepository;
import io.hobaskos.event.repository.search.UserSearchRepository;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class SocialService {
    private final Logger log = LoggerFactory.getLogger(SocialService.class);

    @Inject
    private UsersConnectionRepository usersConnectionRepository;

    @Inject
    private AuthorityRepository authorityRepository;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    private UserSearchRepository userSearchRepository;

    @Inject
    private MailService mailService;

    public void deleteUserSocialConnection(String login) {
        ConnectionRepository connectionRepository = usersConnectionRepository.createConnectionRepository(login);
        connectionRepository.findAllConnections().keySet().stream()
            .forEach(providerId -> {
                connectionRepository.removeConnections(providerId);
                log.debug("Delete user social connection providerId: {}", providerId);
            });
    }

    public void createSocialUser(Connection<?> connection, String langKey) {
        if (connection == null) {
            log.error("Cannot create facebook user because connection is null");
            throw new IllegalArgumentException("Connection cannot be null");
        }
        UserProfile userProfile = connection.fetchUserProfile();
        String providerId = connection.getKey().getProviderId();
        User user = createUserIfNotExist(userProfile, langKey, providerId);
        createSocialConnection(user.getLogin(), connection);
        mailService.sendSocialRegistrationValidationEmail(user, providerId);
    }

    public User createFacebookUser(String accessToken, String langKey) {
        FacebookTemplate facebook = new FacebookTemplate(accessToken);
        log.debug("creating facebook user {}", facebook);
        if (!facebook.isAuthorized()) {
            log.error("Cannot create social user because connection is null");
            throw new IllegalArgumentException("Connection cannot be null");
        }
        String providerId = "facebook";
        UserProfile userProfile = getUserProfile(facebook);
        User user = createUserIfNotExist(userProfile, langKey, providerId);
        userService.createSocialConnection(user, userProfile.getUsername(), accessToken, SocialType.FACEBOOK);
        usersConnectionRepository.createConnectionRepository(user.getLogin());
        mailService.sendSocialRegistrationValidationEmail(user, providerId);

        return user;
    }

    public Optional<User> getUserFromFacebookAccessToken(String accessToken) {
        log.debug("fetching facebook userId {}", accessToken);
        FacebookTemplate facebook = new FacebookTemplate(accessToken);
        if (!facebook.isAuthorized()) {
            log.error("Cannot fetch facebook user because connection is null");
            throw new IllegalArgumentException("Connection cannot be null");
        }

        UserProfile userProfile = getUserProfile(facebook);
        return userService.getSocialUserConnection(SocialType.FACEBOOK.toString(), userProfile.getUsername())
            .map(socialUserConnection -> userRepository.findOneByLogin(socialUserConnection.getUserId()))
            .orElse(Optional.empty());
    }

    private User createUserIfNotExist(UserProfile userProfile, String langKey, String providerId) {
        String email = userProfile.getEmail();
        String userName = userProfile.getUsername();
        if (!StringUtils.isBlank(userName)) {
            userName = userName.toLowerCase(Locale.ENGLISH);
        }
        if (StringUtils.isBlank(email) && StringUtils.isBlank(userName)) {
            log.error("Cannot create social user because email and login are null");
            throw new IllegalArgumentException("Email and login cannot be null");
        }
        if (StringUtils.isBlank(email) && userRepository.findOneByLogin(userName).isPresent()) {
            log.error("Cannot create social user because email is null and login already exist, login -> {}", userName);
            throw new IllegalArgumentException("Email cannot be null with an existing login");
        }
        if (!StringUtils.isBlank(email)) {
            Optional<User> user = userRepository.findOneByEmail(email);
            if (user.isPresent()) {
                log.info("User already exist associate the connection to this account");
                return user.get();
            }
        }

        String login = getLoginDependingOnProviderId(userProfile, providerId);
        String encryptedPassword = passwordEncoder.encode(RandomStringUtils.random(10));
        Set<Authority> authorities = new HashSet<>(1);
        authorities.add(authorityRepository.findOne("ROLE_USER"));

        User newUser = new User();
        newUser.setLogin(login);
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userProfile.getFirstName());
        newUser.setLastName(userProfile.getLastName());
        newUser.setEmail(email);
        newUser.setActivated(true);
        newUser.setAuthorities(authorities);
        newUser.setLangKey(langKey);

        userSearchRepository.save(newUser);
        return userRepository.save(newUser);
    }

    private UserProfile getUserProfile(FacebookTemplate facebookTemplate) {
        String[] fields = { "id", "name", "email", "first_name", "last_name" };
        org.springframework.social.facebook.api.User facebookUserProfile =
            facebookTemplate.fetchObject("me", org.springframework.social.facebook.api.User.class, fields);

        String id = facebookUserProfile.getId();
        String name = facebookUserProfile.getName();
        String firstName = facebookUserProfile.getFirstName();
        String lastName  = facebookUserProfile.getLastName();
        String email = facebookUserProfile.getEmail();
        log.debug("UserProfile id:{}, name:{}, firstName:{}, lastName:{}, email:{}", id, name, firstName, lastName, email);
        return new UserProfile("-1", name, firstName, lastName, email, id);
    }

    /**
     * @return login if provider manage a login like Twitter or Github otherwise email address.
     *         Because provider like Google or Facebook didn't provide login or login like "12099388847393"
     */
    private String getLoginDependingOnProviderId(UserProfile userProfile, String providerId) {
        switch (providerId) {
            case "twitter":
                return userProfile.getUsername().toLowerCase();
            default:
                return userProfile.getEmail();
        }
    }

    private void createSocialConnection(String login, Connection<?> connection) {
        ConnectionRepository connectionRepository = usersConnectionRepository.createConnectionRepository(login);
        connectionRepository.addConnection(connection);
    }
}
