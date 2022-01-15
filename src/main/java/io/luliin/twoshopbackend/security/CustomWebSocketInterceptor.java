package io.luliin.twoshopbackend.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.web.WebInput;
import org.springframework.graphql.web.WebInterceptorChain;
import org.springframework.graphql.web.WebOutput;
import org.springframework.graphql.web.WebSocketInterceptor;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-14
 */
@Slf4j
public class CustomWebSocketInterceptor implements WebSocketInterceptor {


    public Mono<WebOutput> intercept(WebInput webInput, WebInterceptorChain chain) {
        log.info("Webinput headers: {}", webInput.getHeaders());
        log.info("Webinput variables: {}", webInput.getVariables());

        final Mono<WebOutput> next = chain.next(webInput);
        return next.map(webOutput -> {
            Object data =  webOutput.getData();
            log.info("Data {}", data);
            return webOutput.transform(builder -> builder.data(data));
        });
    }


    public Mono<Object> handleConnectionInitialization(Map<String, Object> payload) {

        log.info("Payload: {}", payload);
        return Mono.empty();
    }


}
