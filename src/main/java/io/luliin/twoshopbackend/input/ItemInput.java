package io.luliin.twoshopbackend.input;

import io.luliin.twoshopbackend.entity.Item;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * ItemInput is the Java representation of GraphQL input type ItemInput,
 * which is used when adding or modifying items in a shopping list.
 * This is a composed object of {@link ShoppingListItemInput}.
 * @param name The item name.
 * @param quantity The quantity of items to acquire.
 * @param unit The unit of the acquired item(s)
 * @param isCompleted Will always default to false if omitted.
 *
 * @see ShoppingListItemInput
 * @author Julia Wigenstedt
 * Date: 2022-01-04
 */

public record ItemInput(
        @NotBlank(message = "Ogiltigt namn på produkten")
        @Length(max = 75,
                message = "Namnet får inte innehålla mer än {max} tecken")
        String name,
        Double quantity,
        Item.Unit unit,
        Boolean isCompleted) {
}
