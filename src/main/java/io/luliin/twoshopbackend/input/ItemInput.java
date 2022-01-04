package io.luliin.twoshopbackend.input;

import io.luliin.twoshopbackend.entity.Item;

import javax.validation.constraints.NotBlank;

/**
 * ItemInput is the Java representation of GraphQL input type ItemInput,
 * which is used when adding or modifying items in a shopping list.
 * This is a composed object of {@link ShoppingListItemInput}.
 * @param name The only required field, cannot be null or blank.
 * @param quantity The quantity of items to acquire.
 * @param unit The unit of the acquired item(s)
 * @param isCompleted Will always default to false if omitted.
 *
 * @see ShoppingListItemInput
 * @author Julia Wigenstedt
 * Date: 2022-01-04
 */

public record ItemInput(
        @NotBlank(message = "Ogiltigt namn")
        String name,
        Double quantity,
        Item.Unit unit,
        Boolean isCompleted) {
}
