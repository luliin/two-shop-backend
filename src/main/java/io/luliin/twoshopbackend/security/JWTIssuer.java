package io.luliin.twoshopbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.luliin.twoshopbackend.entity.AppUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWTIssuer is a helper class whose purpose is to generate and validate JWT.
 * @author Julia Wigenstedt
 * Date: 2022-01-13
 */
@Slf4j
public class JWTIssuer {
    private final Key key;
    private final Duration validity;

    public JWTIssuer(final Key key, final Duration validity) {
        this.key = key;
        this.validity = validity;
    }

    /**
     * Generates a JWT for the provided user entity.
     * @param user The authenticated user to create a JWT for.
     * @return The JWT as a String
     */
    public String generateToken(final AppUserEntity user) {

        final String authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("authorities", authorities)
                .signWith(key)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(validity)))
                .compact();
    }


    /**
     * Takes a provided JWT and returns its claims.
     * @param token The (JWT) token to validate.
     * @return The user claims
     */
    public Claims validate(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();
    }

}
