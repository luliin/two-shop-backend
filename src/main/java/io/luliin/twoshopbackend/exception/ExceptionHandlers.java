package io.luliin.twoshopbackend.exception;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Controller;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This exception handler catches some custom validation exceptions and turns them into ValidationErrors,
 * instead of throwing InternalError.
 *
 * @author Julia Wigenstedt
 * Date: 2022-01-22
 */

@Configuration
public class ExceptionHandlers extends DataFetcherExceptionResolverAdapter {

    @Bean
    public DataFetcherExceptionResolver exceptionResolver() {
        return new DataFetcherExceptionResolverAdapter() {
            @Override
            protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
                if (ex instanceof ConstraintViolationException) {
                    return GraphqlErrorBuilder.newError()
                            .errorType(ErrorType.ValidationError)
                            .extensions(getExtensions(((ConstraintViolationException) ex).getConstraintViolations()))
                            .message("Validation errors")
                            .build();
                } else if (ex instanceof CustomValidationException || ex instanceof InvalidEmailException) {
                    return GraphqlErrorBuilder.newError()
                            .errorType(ErrorType.ValidationError)
                            .message(ex.getMessage())
                            .build();
                } else {
                    return super.resolveToSingleError(ex, env);
                }
            }

        };
    }

    private Map<String, Object> getExtensions(Set<ConstraintViolation<?>> constraintViolations) {
        return Map.of("constraintViolations", constraintViolations.stream()
                .map(this::composeErrorExtension)
                .collect(Collectors.toList()));
    }

    private Map<String, Object> composeErrorExtension(ConstraintViolation<?> constraintViolation) {
        Map<String, Object> errorMap = new HashMap<>();

        errorMap.put("messageTemplate", constraintViolation.getMessageTemplate());
        errorMap.put("message", constraintViolation.getMessage());
        errorMap.put("path", composePath(constraintViolation));
        errorMap.put("invalidValue", constraintViolation.getInvalidValue().toString());

        return errorMap;
    }

    private String composePath(ConstraintViolation<?> constraintViolation) {
        Controller annotation = AnnotationUtils.findAnnotation(constraintViolation.getRootBeanClass(), Controller.class);
        if (annotation != null) {
            String propertyPath = constraintViolation.getPropertyPath().toString();
            return propertyPath.substring(propertyPath.indexOf(".") + 1);
        } else {
            return constraintViolation.getPropertyPath().toString();
        }
    }
}

