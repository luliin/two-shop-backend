package io.luliin.twoshopbackend.security;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-13
 */
@Slf4j
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private final JWTIssuer jwtIssuer;

    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JWTIssuer jwtIssuer) {
        super(authenticationManager);
        this.jwtIssuer = jwtIssuer;
    }

    /**
     * Gets the header and authorizes the token inside the header
     * @param request from request by user
     * @param response to send to user
     * @param chain a list of filters
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        final String jwt_token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (jwt_token == null || !jwt_token.startsWith("Bearer")){
            chain.doFilter(request, response);
            return;
        }
        UsernamePasswordAuthenticationToken authenticationToken = getAuthentication(jwt_token);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        chain.doFilter(request, response);
    }

    /**
     * Logs when authentications is unsuccessful
     * @param request from request by user
     * @param response to send to user
     * @param failed authentication exception
     */
    @Override
    protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        log.error("onUnsuccessfulAuthentication {}", failed.getMessage());
        throw failed;
    }

    /**
     * Validates the JWT-token
     * @param jwt_token token from the request with users name and password
     * @return a UsernamePasswordAuthenticationToken object
     */
    public UsernamePasswordAuthenticationToken getAuthentication(String jwt_token){
        Claims claims = jwtIssuer.validate(jwt_token.substring("Bearer ".length()));
        log.info("In getAuthentication() ---  Subject: {}", claims.getSubject());
        log.info("In getAuthentication() ---  Authorities: {}", claims.get("authorities"));

        return new UsernamePasswordAuthenticationToken(claims.getSubject(), claims.getSubject(), getAuthorities(claims));
    }

    /**
     * Gets the authorities and splits them into separate roles
     * @param claims a object with authenticated userinformation
     * @return a collection with SimpleGrantedAuthority
     */
    public Collection<SimpleGrantedAuthority> getAuthorities (Claims claims) {
        String authorities = (String) claims.get("authorities");
        return Arrays.stream(authorities.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
