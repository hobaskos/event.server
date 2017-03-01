package io.hobaskos.event.service;

import io.hobaskos.event.domain.Authority;
import io.hobaskos.event.domain.SocialUserConnection;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.domain.UserConnection;
import io.hobaskos.event.domain.enumeration.SocialType;
import io.hobaskos.event.domain.enumeration.UserConnectionType;
import io.hobaskos.event.repository.AuthorityRepository;
import io.hobaskos.event.repository.SocialUserConnectionRepository;
import io.hobaskos.event.repository.UserConnectionRepository;
import io.hobaskos.event.repository.UserRepository;
import io.hobaskos.event.repository.search.UserSearchRepository;
import io.hobaskos.event.security.AuthoritiesConstants;
import io.hobaskos.event.security.SecurityUtils;
import io.hobaskos.event.service.util.RandomUtil;
import io.hobaskos.event.web.rest.vm.ManagedUserVM;
import io.hobaskos.event.web.rest.vm.SocialAuthVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Inject
    private SocialService socialService;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserSearchRepository userSearchRepository;

    @Inject
    private AuthorityRepository authorityRepository;

    @Inject
    private UserConnectionRepository userConnectionRepository;

    @Inject
    private SocialUserConnectionRepository socialUserConnectionRepository;

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);
                userSearchRepository.save(user);
                log.debug("Activated user: {}", user);
                return user;
            });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
       log.debug("Reset user password for reset key {}", key);

       return userRepository.findOneByResetKey(key)
            .filter(user -> {
                ZonedDateTime oneDayAgo = ZonedDateTime.now().minusHours(24);
                return user.getResetDate().isAfter(oneDayAgo);
           })
           .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                return user;
           });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmail(mail)
            .filter(User::getActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(ZonedDateTime.now());
                return user;
            });
    }

    public User createUser(String login, String password, String firstName, String lastName, String email,
                           String profileImageUrl, String langKey) {

        User newUser = new User();
        Authority authority = authorityRepository.findOne(AuthoritiesConstants.USER);
        Set<Authority> authorities = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(login);
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setProfileImageUrl(profileImageUrl);
        newUser.setLangKey(langKey);
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        authorities.add(authority);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        userSearchRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public User createUser(ManagedUserVM managedUserVM) {
        User user = new User();
        user.setLogin(managedUserVM.getLogin());
        user.setFirstName(managedUserVM.getFirstName());
        user.setLastName(managedUserVM.getLastName());
        user.setEmail(managedUserVM.getEmail());
        user.setProfileImageUrl(managedUserVM.getProfileImageUrl());
        if (managedUserVM.getLangKey() == null) {
            user.setLangKey("en"); // default language
        } else {
            user.setLangKey(managedUserVM.getLangKey());
        }
        if (managedUserVM.getAuthorities() != null) {
            Set<Authority> authorities = new HashSet<>();
            managedUserVM.getAuthorities().forEach(
                authority -> authorities.add(authorityRepository.findOne(authority))
            );
            user.setAuthorities(authorities);
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(ZonedDateTime.now());
        user.setActivated(true);
        userRepository.save(user);
        userSearchRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    public void createSocialConnection(User user, String socialUserId, String accessToken, SocialType type) {
        SocialUserConnection socialUserConnection = new SocialUserConnection();
        socialUserConnection.setUserId(user.getEmail());
        socialUserConnection.setProviderId(type.toString());
        socialUserConnection.setProviderUserId(socialUserId);
        socialUserConnection.setRank(1L);
        socialUserConnection.setDisplayName(user.getLogin());
        socialUserConnection.setAccessToken(accessToken);
        socialUserConnectionRepository.save(socialUserConnection);
    }

    public Optional<SocialUserConnection> getSocialUserConnection(String providerId, String socialUserID) {
        log.debug("socialProvider: {}, socialUserId: {}", providerId, socialUserID);
        return socialUserConnectionRepository.findFirstByProviderIdAndProviderUserId(providerId, socialUserID);
    }

    public void updateUser(String firstName, String lastName, String email, String profileImageUrl, String langKey) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent(user -> {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setProfileImageUrl(profileImageUrl);
            user.setLangKey(langKey);
            userSearchRepository.save(user);
            log.debug("Changed Information for User: {}", user);
        });
    }

    public void updateUser(Long id, String login, String firstName, String lastName, String email, String profileImageUrl,
        boolean activated, String langKey, Set<String> authorities) {

        Optional.of(userRepository
            .findOne(id))
            .ifPresent(user -> {
                user.setLogin(login);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setEmail(email);
                user.setProfileImageUrl(profileImageUrl);
                user.setActivated(activated);
                user.setLangKey(langKey);
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                authorities.forEach(
                    authority -> managedAuthorities.add(authorityRepository.findOne(authority))
                );
                log.debug("Changed Information for User: {}", user);
            });
    }

    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            socialService.deleteUserSocialConnection(user.getLogin());
            userRepository.delete(user);
            userSearchRepository.delete(user);
            log.debug("Deleted User: {}", user);
        });
    }

    public void changePassword(String password) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent(user -> {
            String encryptedPassword = passwordEncoder.encode(password);
            user.setPassword(encryptedPassword);
            log.debug("Changed password for User: {}", user);
        });
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneByLogin(login).map(user -> {
            user.getAuthorities().size();
            return user;
        });
    }

    @Transactional(readOnly = true)
    public User getUserWithAuthorities(Long id) {
        User user = userRepository.findOne(id);
        user.getAuthorities().size(); // eagerly load the association
        return user;
    }

    @Transactional(readOnly = true)
    public User getUserWithAuthorities() {
        Optional<User> optionalUser = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin());
        User user = null;
        if (optionalUser.isPresent()) {
          user = optionalUser.get();
            user.getAuthorities().size(); // eagerly load the association
         }
         return user;
    }


    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     * </p>
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        ZonedDateTime now = ZonedDateTime.now();
        List<User> users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minusDays(3));
        for (User user : users) {
            log.debug("Deleting not activated user {}", user.getLogin());
            userRepository.delete(user);
            userSearchRepository.delete(user);
        }
    }

    /**
     * Get followers for user
     * @param user
     * @return the list of entities
     */
    public Optional<List<User>> getFollowersForUser(User user) {
        return Optional.ofNullable(userConnectionRepository.findByRequesteeAndType(user, UserConnectionType.FOLLOWER))
            .map(userConnections ->
                userConnections.stream().map(UserConnection::getRequester).collect(Collectors.toList())
            );
    }

    /**
     * Get followers for the authenticated user
     * @return the list of entities
     */
    public Optional<List<User>> getFollowers() {
        return getFollowersForUser(getUserWithAuthorities());
    }

    /**
     * Get the ones who follow the user
     * @param user
     * @return the list of entities
     */
    public Optional<List<User>> getFollowingForUser(User user) {
        return Optional.ofNullable(userConnectionRepository.findByRequesterAndType(user, UserConnectionType.FOLLOWER))
            .map(userConnections -> {
                    userConnections.forEach(userConnection -> {
                        userConnection.getRequester().getAuthorities().size(); // eager load authorities
                        userConnection.getRequestee().getAuthorities().size();
                    });
                    return userConnections.stream().map(UserConnection::getRequestee).collect(Collectors.toList());
                }
            );
    }

    /**
     * Get the ones who follow the authenticated user.
     * @return the list of entities
     */
    public Optional<List<User>> getFollowing() {
        return getFollowingForUser(getUserWithAuthorities());
    }

    /**
     * Get user-connections for user with status
     * @param user
     * @return the list of entities
     */
    public Optional<List<User>> getUserConnectionsFor(User user, UserConnectionType type) {
        return Optional.ofNullable(userConnectionRepository.findByRequesterOrRequesteeAndType(user, user, type))
            .map(userConnections -> {
                userConnections.forEach(userConnection -> {
                    userConnection.getRequester().getAuthorities().size(); // eager load authorities
                    userConnection.getRequestee().getAuthorities().size();
                });
                return getUsersFromUserConnections(userConnections, user);
            });
    }

    /**
     * Get user-connections for authenticated user
     * @return
     */
    public Optional<List<User>> getUserConnections(UserConnectionType type) {
        return getUserConnectionsFor(getUserWithAuthorities(), type);
    }

    private List<User> getUsersFromUserConnections(List<UserConnection> userConnections, User user) {
        List<User> users = new ArrayList<>();
        for (UserConnection userConnection : userConnections) {
            if (userConnection.getRequestee().equals(user)) {
                users.add(userConnection.getRequester());
            } else {
                users.add(userConnection.getRequestee());
            }
        }
        return users;
    }
}
