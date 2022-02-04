package io.luliin.twoshopbackend.publisher;

import io.luliin.twoshopbackend.dto.DeletedListResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;

/**
 * This publisher provides subscribers with the requested data when there is an update.
 * @author Julia Wigenstedt
 * Date: 2022-01-21
 */
@Component
@Slf4j
@Getter
@RequiredArgsConstructor
public class DeletedListResponsePublisher {
    private Sinks.Many<DeletedListResponse> deletedListSink;

    @PostConstruct
    private void createShoppingListSubscriptions() {
        deletedListSink = Sinks.many().multicast().directBestEffort();

    }

    @RabbitListener(queues = "#{queue2.name}")
    public void publishDeletedList(DeletedListResponse response) {
        if (response != null) {
            log.info(" >>> ShoppingListService : Publishing message {}", response.message());
            deletedListSink.tryEmitNext(response);
        } else {
            log.error(" >>> An error occurred when publishing deleted list response");
        }
    }

    public Mono<DeletedListResponse> getDeletedListPublisher(Long shoppingListId) {
        return Mono.from(deletedListSink.asFlux()
                .filter(deletedList -> deletedList.shoppingListId().equals(shoppingListId))
                .map(response -> {
                    log.info("Publishing individual subscription for deleted shopping list: {}", shoppingListId);
                    return response;
                })
                .next()).log();
    }


}
