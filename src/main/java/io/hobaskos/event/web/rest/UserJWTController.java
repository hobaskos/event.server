package io.hobaskos.event.web.rest;

import io.hobaskos.event.domain.SocialUserConnection;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.repository.SocialUserConnectionRepository;
import io.hobaskos.event.security.jwt.JWTConfigurer;
import io.hobaskos.event.security.jwt.TokenProvider;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.service.util.RandomUtil;
import io.hobaskos.event.web.rest.vm.JWTTokenVM;
import io.hobaskos.event.web.rest.vm.LoginVM;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.codahale.metrics.annotation.Timed;
import io.hobaskos.event.web.rest.vm.SocialUserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserJWTController {

    private final Logger log = LoggerFactory.getLogger(UserJWTController.class);

    @Inject
    private TokenProvider tokenProvider;

    @Inject
    private AuthenticationManager authenticationManager;

    @Inject
    private UserService userService;

    @Inject
    private SocialUserConnectionRepository socialUserConnectionRepository;

    @PostMapping("/authenticate")
    @Timed
    public ResponseEntity<JWTTokenVM> authorize(@Valid @RequestBody LoginVM loginVM, HttpServletResponse response) {

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(loginVM.getUsername(), loginVM.getPassword());

        try {
            Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            boolean rememberMe = (loginVM.isRememberMe() == null) ? false : loginVM.isRememberMe();
            String jwt = tokenProvider.createToken(authentication, rememberMe);
            response.addHeader(JWTConfigurer.AUTHORIZATION_HEADER, "Bearer " + jwt);
            return ResponseEntity.ok(new JWTTokenVM(jwt));
        } catch (AuthenticationException exception) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/authenticate/social")
    @Timed
    public ResponseEntity<JWTTokenVM> authorize(@Valid @RequestBody SocialUserVM socialUserVM, HttpServletResponse response) {

        boolean newUser = false;
        Optional<SocialUserConnection> users=  socialUserConnectionRepository
            .findFirstByProviderIdAndProviderUserId(socialUserVM.getType().toString(), socialUserVM.getUserId());

        if (!users.isPresent()) {
            newUser = true;
            userService.createUser(socialUserVM);
        }

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(socialUserVM.getUserId(), socialUserVM.getType().toString());

        try {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.createToken(authentication, true);
            response.addHeader(JWTConfigurer.AUTHORIZATION_HEADER, "Bearer " + jwt);
            return new ResponseEntity<>(new JWTTokenVM(jwt), newUser ? HttpStatus.CREATED : HttpStatus.OK);
        } catch (AuthenticationException exception) {
            log.error(exception.getMessage(), exception);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
