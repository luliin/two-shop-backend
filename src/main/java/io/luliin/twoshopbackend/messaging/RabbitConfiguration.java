package io.luliin.twoshopbackend.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-12
 */
@Configuration
public class RabbitConfiguration {

    @Bean
    public TopicExchange topic() {
        return new TopicExchange("topic");
    }

    @Bean
    public TopicExchange mailTopic() {
        return new TopicExchange("mail");
    }

    // Needs to be anonymous, otherwise two instances of this application will receive every other message.
    @Bean
    public Queue queue1() {
        return new AnonymousQueue();
    }

    @Bean
    public Queue queue2() {
        return new AnonymousQueue();
    }

    @Bean
    public Queue mailQueue() {
        return new Queue("mail");
    }


    @Bean
    public Binding forwardBinding(TopicExchange topic,
                                   Queue queue1) {
        return BindingBuilder.bind(queue1)
                .to(topic)
                .with("forwarded.*");
    }

    @Bean
    public Binding deletedBinding(TopicExchange topic,
                                  Queue queue2) {
        return BindingBuilder.bind(queue2)
                .to(topic)
                .with("deleted.*");
    }

    @Bean
    public Binding mailBinding() {
        return BindingBuilder.bind(mailQueue())
                .to(mailTopic())
                .with("confirm.*");
    }


    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory, final Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
