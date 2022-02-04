package io.luliin.twoshopbackend.dto;

/**
 * This is the DTO returned when calling all user update mutations.
 * ModifiedAppUser is the POJO equivalent of the GraphQL type ModifiedAppUser.
 * @param appUser The modified user
 * @param message Provided by service method depending on which mutation was called
 * @author Julia Wigenstedt
 * Date: 2022-01-22
 */
public record ModifiedAppUser(
        AppUser appUser,
        String message
) {
}
