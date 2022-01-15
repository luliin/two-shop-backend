package io.luliin.twoshopbackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.luliin.twoshopbackend.dto.LoginResponse;
import io.luliin.twoshopbackend.entity.AppUserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;



/**
 * @author Julia Wigenstedt
 * Date: 2022-01-13
 */

@RequiredArgsConstructor
@Slf4j
public class AuthFilter extends UsernamePasswordAuthenticationFilter {

    private final JWTIssuer jwtIssuer;
    private final AuthenticationManager authenticationManager;


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        final String username = request.getParameter("username");
        final String password = request.getParameter("password");
        log.info("Authenticating user {}", username);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        final Authentication authenticate = authenticationManager.authenticate(token);
        log.info("Authenticated user {}", authenticate.getCredentials());
        return authenticate;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {

        AppUserEntity user = (AppUserEntity) authResult.getPrincipal();
        String token = jwtIssuer.generateToken(user);
        String formatCookie = "Bearer " + token;

        response.setHeader(HttpHeaders.AUTHORIZATION, formatCookie);
        response.addCookie(new Cookie("jwt_token",token));

        LoginResponse loginResponseDto = new LoginResponse(user.getUsername(),
                LoginResponse.convertSimpleGrantedAuthority((Collection<GrantedAuthority>) user.getAuthorities()),
                "Bearer " + token);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            new ObjectMapper().writeValue(response.getOutputStream(), loginResponseDto);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
