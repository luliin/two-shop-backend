package io.luliin.twoshopbackend.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.web.WebSocketInterceptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;



/**
 * @author Julia Wigenstedt
 * Date: 2022-01-13
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${security.signingKey}")
    private String signingKey;

    @Value("${security.algorithm}")
    private String algorithm;

    @Value("${security.validMinutes}")
    private Integer validMinutes;

    private final AppUserRepository appUserRepository;


    @Bean
    public JWTIssuer jwtIssuer() {
        final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.forName(algorithm);
        final byte[] signingKeyBytes = Base64.encodeBase64(signingKey.getBytes());
        return new JWTIssuer(new SecretKeySpec(signingKeyBytes, signatureAlgorithm.getJcaName()), Duration.ofMinutes(validMinutes));
    }

    @Bean
    public PasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(10);
    }



    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(username -> appUserRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found")))
                .passwordEncoder(bCryptPasswordEncoder());
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        final JWTAuthorizationFilter jwtAuthorizationFilter = new JWTAuthorizationFilter(authenticationManager(), jwtIssuer());
        final AuthFilter authenticationFilter = new AuthFilter(jwtIssuer(), authenticationManager());

        http.cors().and().csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .addFilterBefore(authenticationFilter, JWTAuthorizationFilter.class)
                .addFilter(jwtAuthorizationFilter)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }


    @Bean
    public WebSocketInterceptor webSocketInterceptor() {
        return new CustomWebSocketInterceptor();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        // This prevents auto generated security password
        return super.authenticationManagerBean();
    }








}
