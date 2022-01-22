package io.luliin.twoshopbackend.exception;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;

import java.util.List;

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

