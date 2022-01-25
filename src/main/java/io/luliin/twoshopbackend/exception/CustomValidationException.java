package io.luliin.twoshopbackend.exception;

import graphql.ErrorType;

/**
 * This exception can be thrown when some custom validation fails.
 * An exception handler will turn it into a graphQl {@link ErrorType}.ValidationError
 * @author Julia Wigenstedt
 * Date: 2022-01-22
 */
public class CustomValidationException extends RuntimeException {
    public CustomValidationException(String message) {
        super(message);
    }
}
