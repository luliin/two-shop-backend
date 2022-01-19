package io.luliin.twoshopbackend.security;


import graphql.ExecutionInput;
import graphql.GraphQLContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.web.WebInput;
import org.springframework.graphql.web.WebInterceptorChain;
import org.springframework.graphql.web.WebOutput;
import org.springframework.graphql.web.WebSocketInterceptor;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNullApi;

import java.util.Collections;
import java.util.Map;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-14
 */
@Slf4j
public class CustomWebSocketInterceptor implements WebSocketInterceptor {

    @Override
    public Mono<WebOutput> intercept(WebInput webInput, WebInterceptorChain chain) {
        log.info("Webinput headers: {}", webInput.getHeaders());
        log.info("Webinput variables: {}", webInput.getVariables());
        var test = webInput.toExecutionInput(true).toString();
        log.info("GraphQlContext {}", test);
        final Mono<WebOutput> next = chain.next(webInput);
        return next.map(webOutput -> {
            Object data = webOutput.getData();
            log.info("Data {}", data);
            return webOutput.transform(builder -> builder.data(data));
        });
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
