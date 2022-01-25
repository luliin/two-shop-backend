package io.luliin.twoshopbackend.exception;

import graphql.ErrorType;
import graphql.GraphqlErrorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-22
 */

@Configuration
public class ExceptionHandlers extends DataFetcherExceptionResolverAdapter {

    @Bean
    public DataFetcherExceptionResolver resolveException() {
        return DataFetcherExceptionResolverAdapter.from((ex, env) -> {
            if(ex instanceof InvalidEmailException || ex instanceof CustomValidationException) {
               return GraphqlErrorBuilder.newError(env)
                        .message(ex.getMessage())
                        .errorType(ErrorType.ValidationError)
                        .build();
            } else return null;
        });
    }
}

