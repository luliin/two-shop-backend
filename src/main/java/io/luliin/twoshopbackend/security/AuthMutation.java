package io.luliin.twoshopbackend.security;

import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.dto.AuthenticationPayload;
import io.luliin.twoshopbackend.entity.AppUserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * AuthMutation is a class that provides the possibility to authenticate through a mutation,<br>
 * instead of making a REST-call through "/login".
 *
 * @author Julia Wigenstedt
 * Date: 2022-01-25
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class AuthMutation {

    private final JWTIssuer jwtIssuer;
    private final AuthenticationManager authenticationManager;

    /**
     *
     * @param username The username of the user making the request
     * @param password The password of the user making the request
     * @return An {@link AuthenticationPayload} with JWT and the authenticated user as an {@link AppUser}
     * @throws AuthenticationException If bad credentials
     */
    public AuthenticationPayload attemptAuthenticationMutation(String username, String password) throws AuthenticationException {
        log.info("Authenticating user from mutation {}", username);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        final Authentication authenticate = authenticationManager.authenticate(token);
        log.info("Authenticated user from mutation {}", authenticate.getName());

        AppUserEntity user = (AppUserEntity) authenticate.getPrincipal();
        String JWT = "Bearer " + jwtIssuer.generateToken(user);
        return new AuthenticationPayload(JWT, user.toAppUser());
    }
}
