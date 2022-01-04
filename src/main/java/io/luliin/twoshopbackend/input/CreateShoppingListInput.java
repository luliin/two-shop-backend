package io.luliin.twoshopbackend.input;

import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.entity.ShoppingList;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * CreateShoppingListInput is the Java representation of the GraphQL input type CreateShoppingListInput,
 * which is the expected input when persisting a new {@link ShoppingList} to the database.
 * Basic validation on the fields are provided, along with corresponding error messages in swedish.
 * @param ownerCredential Email or username of {@link AppUser} owner.
 * @param collaboratorCredential Email or username of {@link AppUser} collaborator, if there is one.
 * @param name The name of the shopping list
 * @author Julia Wigenstedt
 * Date: 2022-01-04
 */
public record CreateShoppingListInput(
        @NotBlank
        String ownerCredential,
        String collaboratorCredential,
        @NotBlank(message = "Ogiltigt lösenord")
        @Length(max = 100, message = "Namnet får inte innehålla mer än {max} tecken")
        String name) {
}
