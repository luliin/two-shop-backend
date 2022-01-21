package io.luliin.twoshopbackend.publisher;

import graphql.schema.DataFetchingEnvironment;
import io.luliin.twoshopbackend.entity.ShoppingList;
import io.luliin.twoshopbackend.messaging.RabbitSender;
import io.luliin.twoshopbackend.repository.ShoppingListRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-07
 */
@RequiredArgsConstructor
@Getter
@Slf4j
@Component
public class ShoppingListPublisher {

    private Sinks.Many<ShoppingList> shoppingListSink;

    private final RabbitSender rabbitSender;

    private final ShoppingListRepository shoppingListRepository;

    @PostConstruct
    private void createShoppingListSubscriptions() {
        shoppingListSink = Sinks.many().multicast().directBestEffort();
    }


    @RabbitListener(queues = "#{queue1.name}")
    public void publishShoppingListItems(Long shoppingListId) {
        ShoppingList shoppingList = shoppingListRepository
                .findById(shoppingListId)
                .orElse(null);
        if (shoppingList != null) {
            log.info(" >>> ShoppingListService : Shopping list {} updated", shoppingListId);
            shoppingListSink.tryEmitNext(shoppingList);
        } else {
            log.info(" >>> There is no shopping list with id: {}", shoppingListId);
        }

    }


    public Flux<ShoppingList> getShoppingListPublisher(Long shoppingListId, DataFetchingEnvironment environment) {
        log.info("In getShoppingListPublisher {}", shoppingListId);
        environment.getArguments().forEach((a, b) -> log.info("Arguments: {}={}", a, b));

        return  shoppingListSink.asFlux()
                .filter(shoppingList -> shoppingListId.equals(shoppingList.getId()))
                .map(shoppingList -> {
                    log.info("Publishing individual subscription update for Shopping list {}", shoppingList.getName());
                    return shoppingList;
                }).log();
    }


}
