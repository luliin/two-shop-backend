package io.luliin.twoshopbackend.exception;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-22
 */
public class CustomValidationException extends RuntimeException {
    public CustomValidationException(String message) {
        super(message);
    }
}
