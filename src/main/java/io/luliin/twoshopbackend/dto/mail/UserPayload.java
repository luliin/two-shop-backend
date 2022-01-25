package io.luliin.twoshopbackend.dto.mail;

/**
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
