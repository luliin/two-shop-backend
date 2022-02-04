package io.luliin.twoshopbackend.security;


import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.web.WebInput;
import org.springframework.graphql.web.WebInterceptorChain;
import org.springframework.graphql.web.WebOutput;
import org.springframework.graphql.web.WebSocketInterceptor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

/**
 * CustomWebSocketInterceptor is an implementation of WebSocketInterceptor,
 * which is a part of the WebInterceptorChain.
 * @author Julia Wigenstedt
 * Date: 2022-01-14
 */
@Slf4j
public class CustomWebSocketInterceptor implements WebSocketInterceptor {

    @Override
    public Mono<WebOutput> intercept(@NonNull WebInput webInput, WebInterceptorChain chain) {
        final Mono<WebOutput> next = chain.next(webInput);

        return next.map(webOutput -> {
            Object data = webOutput.getData();
            HttpHeaders responseHeaders = webOutput.getResponseHeaders();
            log.info("Data {}", data);
            return webOutput.transform(builder -> builder.data(data));
        }).log();
    }


    public Mono<Object> handleConnectionInitialization(Map<String, Object> payload) {

        log.info("Payload: {}", payload);
        return Mono.just(Collections.singletonMap("JWT", "name"));

    }


    /**
     * Handle the completion message that a GraphQL over WebSocket clients sends
     * before closing the WebSocket connection.
     *
     * @return signals the end of completion handling
     */
    public Mono<Void> handleConnectionCompletion() {
        log.info("In connection complete");
        return Mono.empty();
    }


}
