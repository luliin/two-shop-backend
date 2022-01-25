package io.luliin.twoshopbackend.dto;


/**
 * This is a DTO that is used as a nested collection of objects in {@link LoginResponse}.
 * @param name The name of the role.
 * @author Julia Wigenstedt
 * Date: 2022-01-13
 */
public record RoleResponse(String name) {
}
