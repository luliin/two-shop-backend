package io.luliin.twoshopbackend.dto;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-22
 */
public record ModifiedAppUser(
        AppUser appUser,
        String message
) {
}
