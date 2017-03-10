package io.hobaskos.event.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.security.UserDetailsService;
import io.hobaskos.event.security.jwt.JWTConfigurer;
import io.hobaskos.event.security.jwt.TokenProvider;
import io.hobaskos.event.service.SocialService;
import io.hobaskos.event.service.UserService;
import io.hobaskos.event.web.rest.vm.JWTTokenVM;
import io.hobaskos.event.web.rest.vm.LoginVM;
import io.hobaskos.event.web.rest.vm.SocialAuthVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private SocialService socialService;


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

    /**
     * POST /authenticate/social : Only applicable for facebook!  | Authenticates or creates an account
     * @param socialAuthVM
     * @param response
     * @return
     */
    @PostMapping("/authenticate/social")
    @Timed
    public ResponseEntity<JWTTokenVM> authorizeSocial(@Valid @RequestBody SocialAuthVM socialAuthVM,
                                                HttpServletResponse response) {

        Optional<User> user = socialService.getUserFromFacebookAccessToken(socialAuthVM.getAccessToken());
        UsernamePasswordAuthenticationToken authenticationToken;

        if (user.isPresent()) {
            authenticationToken =
                new UsernamePasswordAuthenticationToken(user.get().getLogin(), null, user.get().getAuthorities().stream()
                    .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                    .collect(Collectors.toList()));
        } else {
            User newUser = socialService.createFacebookUser(socialAuthVM.getAccessToken(),
            socialAuthVM.getLangKey());
            authenticationToken =
                new UsernamePasswordAuthenticationToken(newUser.getLogin(), null, newUser.getAuthorities().stream()
                    .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                    .collect(Collectors.toList()));
        }
        return authenticateSocial(authenticationToken, response);
    }

    private ResponseEntity<JWTTokenVM> authenticateSocial(UsernamePasswordAuthenticationToken upat, HttpServletResponse response) {
        try {
            SecurityContextHolder.getContext().setAuthentication(upat);
            SecurityContextHolder.getContext().setAuthentication(upat);
            String jwt = tokenProvider.createToken(upat, true);
            response.addHeader(JWTConfigurer.AUTHORIZATION_HEADER, "Bearer " + jwt);
            return new ResponseEntity<>(new JWTTokenVM(jwt), HttpStatus.OK);
        } catch (AuthenticationException exception) {
            log.error(exception.getMessage(), exception);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
