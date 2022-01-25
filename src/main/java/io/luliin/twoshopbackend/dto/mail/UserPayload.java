package io.luliin.twoshopbackend.dto.mail;

/**
 * A DTO to use when populating content in dynamic email templates.
 * @param username The recipient's username.
 * @param email The recipient's email.
 * @param firstName The recipient's first name.
 * @param lastName The recipient's last name.
 * @param updatedPassword The recipient's updated password (if update password mutation is called).
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
