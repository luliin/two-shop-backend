package io.luliin.twoshopbackend.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-24
 */
@Configuration
public class MailConfiguration {

    @Value("${security.welcomeURL}")
    private String welcomeUrl;

    @Bean
    public MailSender mailSender() {
        return new MailSender(welcomeUrl);
    }
}
