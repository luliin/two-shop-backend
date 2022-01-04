package io.luliin.twoshopbackend.input;

import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.entity.ShoppingList;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * ShoppingListItemInput is the Java representation of GraphQL input type ShoppingListItemInput,
 * which is used when adding or modifying items in a shopping list.
 * @param userCredentials The email or username of the {@link AppUser} making the request.
 * @param shoppingListId The id of the {@link ShoppingList} to modify.
 * @param itemInput The {@link ItemInput} representing the item to add or modify.
 * @author Julia Wigenstedt
 * Date: 2022-01-04
 */
public record ShoppingListItemInput(
        @NotBlank
        String userCredentials,
        @NotNull
        Long shoppingListId,
        @Valid
        ItemInput itemInput) {
}
