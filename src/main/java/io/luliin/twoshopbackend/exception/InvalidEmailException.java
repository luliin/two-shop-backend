package io.luliin.twoshopbackend.exception;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-22
 */
public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String message) {
        super(message);
    }
}
