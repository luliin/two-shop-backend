package io.luliin.twoshopbackend.input;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-15
 */
public record HandleCollaboratorInput(
        @NotNull(message = "Unspecified list!")
        Long shoppingListId,
        @Email(message = "You must provide a valid email for the collaborator!")
        String collaboratorCredential) {
}
