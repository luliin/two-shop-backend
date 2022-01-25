package io.luliin.twoshopbackend.mail;

/**
 * This is a DTO received from mail service through message broker when an email has been sent.
 * @param message The message from the mail service.
 * @author Julia Wigenstedt
 * Date: 2022-01-23
 */
public record EmailResponse(String message) {
}
