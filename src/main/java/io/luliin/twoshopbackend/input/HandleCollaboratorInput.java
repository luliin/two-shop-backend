package io.luliin.twoshopbackend.input;

import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.ShoppingList;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * HandleCollaboratorInput is the Java representation of the GraphQL input type HandleCollaboratorInput,
 * which is the expected input when inviting a new collaborator ({@link AppUserEntity}) to a {@link ShoppingList}.
 * Basic validation on the fields are provided, along with corresponding error messages in swedish.
 * @param shoppingListId The id of the list to add the collaborator to.
 * @param collaboratorCredential The new collaborator's credential, either username or email.
 * @author Julia Wigenstedt
 * Date: 2022-01-15
 */
public record HandleCollaboratorInput(
        @NotNull(message = "Du måste ange vilken shoppinglista som ska få en collaborator!")
        Long shoppingListId,
        @NotBlank(message = "Du måste ange en giltig e-post eller användarnamn till collaboratorn!")
        String collaboratorCredential) {
}
