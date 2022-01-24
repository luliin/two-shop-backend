package io.luliin.twoshopbackend.messaging;

import io.luliin.twoshopbackend.mail.EmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-24
 */
@Component
@Slf4j
public class MessageListener {

    @RabbitListener(queues = "mail")
    public void receiveWelcomeRequest(EmailResponse emailResponse) {
        log.info("Received confirmation response with message: {}", emailResponse.message());
    }
}
