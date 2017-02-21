package io.hobaskos.event.web.rest;

import com.codahale.metrics.annotation.Timed;

import io.hobaskos.event.config.Constants;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.domain.UserConnection;
import io.hobaskos.event.domain.enumeration.UserConnectionType;
import io.hobaskos.event.repository.UserRepository;
import io.hobaskos.event.security.SecurityUtils;
import io.hobaskos.event.service.MailService;
import io.hobaskos.event.service.UserConnectionService;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.dto.UserConnectionDTO;
import io.hobaskos.event.service.dto.UserDTO;
import io.hobaskos.event.web.rest.util.PaginationUtil;
import io.hobaskos.event.web.rest.vm.KeyAndPasswordVM;
import io.hobaskos.event.web.rest.vm.ManagedUserVM;
import io.hobaskos.event.web.rest.util.HeaderUtil;

import io.reactivex.exceptions.Exceptions;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    private MailService mailService;

    @Inject
    private UserConnectionService userConnectionService;

    /**
     * POST  /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     * @return the ResponseEntity with status 201 (Created) if the user is registered or 400 (Bad Request) if the login or e-mail is already in use
     */
    @PostMapping(path = "/register",
                    produces={MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Timed
    public ResponseEntity<?> registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {

        HttpHeaders textPlainHeaders = new HttpHeaders();
        textPlainHeaders.setContentType(MediaType.TEXT_PLAIN);

        return userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase())
            .map(user -> new ResponseEntity<>("login already in use", textPlainHeaders, HttpStatus.BAD_REQUEST))
            .orElseGet(() -> userRepository.findOneByEmail(managedUserVM.getEmail())
                .map(user -> new ResponseEntity<>("e-mail address already in use", textPlainHeaders, HttpStatus.BAD_REQUEST))
                .orElseGet(() -> {
                    User user = userService
                        .createUser(managedUserVM.getLogin(), managedUserVM.getPassword(),
                            managedUserVM.getFirstName(), managedUserVM.getLastName(),
                            managedUserVM.getEmail().toLowerCase(), managedUserVM.getProfileImageUrl(),
                            managedUserVM.getLangKey());

                    mailService.sendActivationEmail(user);
                    return new ResponseEntity<>(HttpStatus.CREATED);
                })
        );
    }

    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @return the ResponseEntity with status 200 (OK) and the activated user in body, or status 500 (Internal Server Error) if the user couldn't be activated
     */
    @GetMapping("/activate")
    @Timed
    public ResponseEntity<String> activateAccount(@RequestParam(value = "key") String key) {
        return userService.activateRegistration(key)
            .map(user -> new ResponseEntity<String>(HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * GET  /authenticate : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping("/authenticate")
    @Timed
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * GET  /account : get the current user.
     *
     * @return the ResponseEntity with status 200 (OK) and the current user in body, or status 500 (Internal Server Error) if the user couldn't be returned
     */
    @GetMapping("/account")
    @Timed
    public ResponseEntity<UserDTO> getAccount() {
        return Optional.ofNullable(userService.getUserWithAuthorities())
            .map(user -> new ResponseEntity<>(new UserDTO(user), HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * POST  /account : update the current user information.
     *
     * @param userDTO the current user information
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) or 500 (Internal Server Error) if the user couldn't be updated
     */
    @PostMapping("/account")
    @Timed
    public ResponseEntity<String> saveAccount(@Valid @RequestBody UserDTO userDTO) {
        Optional<User> existingUser = userRepository.findOneByEmail(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userDTO.getLogin()))) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("user-management", "emailexists", "Email already in use")).body(null);
        }
        return userRepository
            .findOneByLogin(SecurityUtils.getCurrentUserLogin())
            .map(u -> {
                userService.updateUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(),
                    userDTO.getProfileImageUrl(), userDTO.getLangKey());
                return new ResponseEntity<String>(HttpStatus.OK);
            })
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * POST  /account/change_password : changes the current user's password
     *
     * @param password the new password
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) if the new password is not strong enough
     */
    @PostMapping(path = "/account/change_password",
        produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<?> changePassword(@RequestBody String password) {
        if (!checkPasswordLength(password)) {
            return new ResponseEntity<>("Incorrect password", HttpStatus.BAD_REQUEST);
        }
        userService.changePassword(password);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * POST   /account/reset_password/init : Send an e-mail to reset the password of the user
     *
     * @param mail the mail of the user
     * @return the ResponseEntity with status 200 (OK) if the e-mail was sent, or status 400 (Bad Request) if the e-mail address is not registered
     */
    @PostMapping(path = "/account/reset_password/init",
        produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<?> requestPasswordReset(@RequestBody String mail) {
        return userService.requestPasswordReset(mail)
            .map(user -> {
                mailService.sendPasswordResetMail(user);
                return new ResponseEntity<>("e-mail was sent", HttpStatus.OK);
            }).orElse(new ResponseEntity<>("e-mail address not registered", HttpStatus.BAD_REQUEST));
    }

    /**
     * POST   /account/reset_password/finish : Finish to reset the password of the user
     *
     * @param keyAndPassword the generated key and the new password
     * @return the ResponseEntity with status 200 (OK) if the password has been reset,
     * or status 400 (Bad Request) or 500 (Internal Server Error) if the password could not be reset
     */
    @PostMapping(path = "/account/reset_password/finish",
        produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<String> finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (!checkPasswordLength(keyAndPassword.getNewPassword())) {
            return new ResponseEntity<>("Incorrect password", HttpStatus.BAD_REQUEST);
        }
        return userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey())
              .map(user -> new ResponseEntity<String>(HttpStatus.OK))
              .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private boolean checkPasswordLength(String password) {
        return (!StringUtils.isEmpty(password) &&
            password.length() >= ManagedUserVM.PASSWORD_MIN_LENGTH &&
            password.length() <= ManagedUserVM.PASSWORD_MAX_LENGTH);
    }

    /**
     * GET /account/followers
     * @return
     */
    @GetMapping(path = "/account/followers", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Timed
    public ResponseEntity<List<UserDTO>> getFollowers(@ApiParam Pageable pageable) {
        log.debug("REST request to get Followers for account {}");
        return userService.getFollowers()
            .map(users ->  users.stream().map(UserDTO::new).collect(Collectors.toList()))
            .map(result -> {
                try {
                    Page<UserDTO> page = new PageImpl<>(result, pageable, result.size());
                    HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/account/followers");
                    return new ResponseEntity<>(result, headers, HttpStatus.OK);
                } catch (URISyntaxException e) { throw Exceptions.propagate(e); }
            })
            .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    /**
     * GET /account/following
     * @return
     */
    @GetMapping(path = "/account/following", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<UserDTO>> getFollowing(@ApiParam Pageable pageable) {
        log.debug("REST request to get Followers for account {}");
        return userService.getFollowing()
            .map(users ->  users.stream().map(UserDTO::new).collect(Collectors.toList()))
            .map(result -> {
                try {
                    Page<UserDTO> page = new PageImpl<>(result, pageable, result.size());
                    HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/account/following");
                    return new ResponseEntity<>(result, headers, HttpStatus.OK);
                } catch (URISyntaxException e) { throw Exceptions.propagate(e); }
            })
            .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    /**
     * POST /account/following/{login}
     * @param login
     * @return
     */
    @PostMapping(path = "/account/following/{login:" + Constants.LOGIN_REGEX + "}",
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<UserConnectionDTO> follow(@PathVariable String login) {
        return Optional.ofNullable(userService.getUserWithAuthoritiesByLogin(login))
            .map(user -> {
                UserConnectionDTO userConnectionDTO = userConnectionService
                    .makeFollowingConnection(userService.getUserWithAuthorities(), user.get());
                return new ResponseEntity<>(userConnectionDTO, HttpStatus.OK);
            })
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET /account/user-connections
     * @param type
     * @param pageable
     * @return
     */
    @GetMapping(path = "/account/user-connections", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<UserDTO>> getUserConnections(@RequestParam UserConnectionType type, @ApiParam Pageable pageable) {
        log.debug("REST request to get Friends for account {}");
        return userService.getUserConnections(type)
            .map(users ->  users.stream().map(UserDTO::new).collect(Collectors.toList()))
            .map(result -> {
                try {
                    Page<UserDTO> page = new PageImpl<>(result, pageable, result.size());
                    HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/account/friends");
                    return new ResponseEntity<>(result, headers, HttpStatus.OK);
                } catch (URISyntaxException e) { throw Exceptions.propagate(e); }
            })
            .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    /**
     * POST /account/user-connections/{login} : make friends with user login
     * @param login
     * @return
     */
    @PostMapping(path = "/account/user-connections/{login:" + Constants.LOGIN_REGEX + "}",
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<UserConnectionDTO> makeUserConnection(@PathVariable String login) {
        log.debug("REST request to make friends with {}", login);

        return Optional.ofNullable(userService.getUserWithAuthoritiesByLogin(login))
            .map(user -> {
                UserConnectionDTO userConnectionDTO = userConnectionService
                    .makeConnection(userService.getUserWithAuthorities(), user.get());
                return new ResponseEntity<>(userConnectionDTO, HttpStatus.OK);
            })
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
