package io.luliin.twoshopbackend.exception;

import graphql.ErrorType;

/**
 * This exception can be thrown when email validation fails.
 * An exception handler will turn it into a graphQl {@link ErrorType}.ValidationError
 * @author Julia Wigenstedt
 * Date: 2022-01-22
 */
public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String message) {
        super(message);
    }
}
