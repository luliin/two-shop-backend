package io.luliin.twoshopbackend.input;

import javax.validation.constraints.NotBlank;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-04
 */
public record CreateShoppingListInput(
        @NotBlank
        String ownerCredential,
        String collaboratorCredential,
        @NotBlank
        String name) {
}
