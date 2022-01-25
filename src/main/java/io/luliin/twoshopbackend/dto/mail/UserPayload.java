package io.luliin.twoshopbackend.dto.mail;

/**
 * A DTO to use when populating content in dynamic email templates.
 * @author Julia Wigenstedt
 * Date: 2022-01-23
 */
public record UserPayload(
        String username,
        String email,
        String firstName,
        String lastName,
        String updatedPassword
) {
}
