package io.luliin.twoshopbackend.messaging;

import io.luliin.twoshopbackend.dto.DeletedListResponse;
import io.luliin.twoshopbackend.entity.ShoppingList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitSender is a helper class designed to make subscriptions somewhat scalable.
 * Use in services where subscriptions need to be emitted, by calling its methods
 * to forward the {@link ShoppingList}'s id, or a {@link DeletedListResponse} to the message broker using autowired
 * {@link RabbitTemplate} and {@link TopicExchange}.
 *
 * @author Julia Wigenstedt
 * Date: 2022-01-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitSender {

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange topic;

    /**
     * Publishes a shopping list's id to the message broker,
     * making sure all instances of the application will receive and emit subscriptions.
     * Use {@link RabbitListener} to listen for messages.
     * @param shoppingList The ShoppingList being modified
     */
    public void publishShoppingListSubscription(ShoppingList shoppingList) {
        String routingKey = "forwarded.message";
        rabbitTemplate.convertAndSend(topic.getName(), routingKey, shoppingList.getId());
        log.info(" [x] Published subscription info on shoppingListId {}", shoppingList.getId());
    }

    /**
     * Publishes a deleted list response to the message broker,
     * making sure all instances of the application will receive and emit subscriptions.
     * Use {@link RabbitListener} to listen for messages.
     * @param deletedListResponse The payload created when deleting the shopping list
     */
    public void publishDeletedResponse(DeletedListResponse deletedListResponse) {
        String routingKey = "deleted.message";
        rabbitTemplate.convertAndSend(topic.getName(), routingKey, deletedListResponse);
        log.info(" [x] Published subscription info on deleted shopping list with id {}", deletedListResponse.shoppingListId());
    }
}
