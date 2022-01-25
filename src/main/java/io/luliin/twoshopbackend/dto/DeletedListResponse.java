package io.luliin.twoshopbackend.dto;

/**
 * This is the DTO returned when a successful delete list mutation is called.
 * DeletedListResponse is the POJO equivalent of the GraphQL type DeletedListResponse.
 * @param message The message provided to client after list is deleted.
 * @param path The path client redirects to after list is deleted (if user is in said Shopping list view).
 * @param shoppingListId The id of the shopping list that was deleted.
 * @author Julia Wigenstedt
 * Date: 2022-01-17
 */
public record DeletedListResponse(
        String message,
        String path,
        Long shoppingListId
) {
}
