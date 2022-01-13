package io.luliin.twoshopbackend.security;

import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
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
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${security.signingKey}")
    private String signingKey;

    @Value("${security.algorithm}")
    private String algorithm;

    @Value("${security.validMinutes}")
    private Integer validMinutes;

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




}
