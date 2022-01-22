package io.luliin.twoshopbackend.dto;

import io.luliin.twoshopbackend.entity.ShoppingList;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-16
 */
public record ModifiedShoppingList(
        ShoppingList shoppingList,
        String message
) {
}
