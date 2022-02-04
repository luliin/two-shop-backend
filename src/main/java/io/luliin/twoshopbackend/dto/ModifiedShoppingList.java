package io.luliin.twoshopbackend.dto;

import io.luliin.twoshopbackend.entity.ShoppingList;

/**
 * This is the DTO returned when calling all ShoppingList update mutations,<br>
 * except from delete mutation.
 * ModifiedShoppingList is the POJO equivalent of the GraphQL type ModifiedShoppingList.
 * @param shoppingList The shopping list that was modified.
 * @param message The message provided by service method depending on what mutation was called.
 * @author Julia Wigenstedt
 * Date: 2022-01-16
 */
public record ModifiedShoppingList(
        ShoppingList shoppingList,
        String message
) {
}
