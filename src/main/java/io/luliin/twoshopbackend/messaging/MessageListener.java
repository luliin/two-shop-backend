package io.luliin.twoshopbackend.messaging;

import io.luliin.twoshopbackend.dto.mail.EmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * This components methods utilizes RabbitMQ's RabbitListener to listen for messages sent from mail service.
 * @author Julia Wigenstedt
 * Date: 2022-01-24
 */
@Component
@Slf4j
public class MessageListener {

    /**
     * Listens for messages from mail queue and logs them.
     * @param emailResponse The response provided by mail service
     */
    @RabbitListener(queues = "mail")
    public void receiveWelcomeRequest(EmailResponse emailResponse) {
        log.info("Received confirmation response with message: {}", emailResponse.message());
    }
}
