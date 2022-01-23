package io.luliin.twoshopbackend.mail;

import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.dto.mail.UserPayload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-23
 */
@Service
@Slf4j
public class MailSender {

    private final RestTemplate restTemplate;

    @Value("${mail.welcome-url}")
    private String welcomeUrl;

    public MailSender() {
        this.restTemplate = new RestTemplate();
    }

    public String sendWelcomeMessage(AppUser appUser) {

        UserPayload userPayload = new UserPayload(appUser.getUsername(),
                appUser.getEmail(),
                appUser.getFirstName(),
                appUser.getLastName(),
                null);

        final ResponseEntity<EmailResponse> emailResponseResponseEntity =
                this.restTemplate.postForEntity(welcomeUrl, appUser, EmailResponse.class);
        log.info("Email sent with response body {}", emailResponseResponseEntity.getBody());
        var statusCode = emailResponseResponseEntity.getStatusCodeValue();
        if(statusCode < 200 || statusCode > 299) {
            var message = "An error occurred when sending email. " +
                    "Status code was: " + statusCode + ", body: " + emailResponseResponseEntity.getBody();
            log.error(message);
            return message;
        }
        String message = "An email was sent to " + userPayload.email();
        log.info("Message: {}", message);
        return message;
    }
}
