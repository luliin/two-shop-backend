package io.luliin.twoshopbackend.dto;

/**
 * This is the DTO returned when a successful login mutation is called.
 * AuthenticationPayload is the POJO equivalent of the GraphQL type AuthenticationPayload.
 * @param jwt The generated JWT, to use in subsequent requests
 * @param appUser The authenticated user
 * @author Julia Wigenstedt
 * Date: 2022-01-25
 */
public record AuthenticationPayload(String jwt, AppUser appUser) {
}
