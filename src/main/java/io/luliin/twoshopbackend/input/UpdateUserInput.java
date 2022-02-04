package io.luliin.twoshopbackend.input;

import java.util.List;
import java.util.Optional;

/**
 * UpdateUserInput is a POJO representation of GraphQl input type with the same name,
 * and is the expected input type used when calling updateUser mutation.
 * @param updatedEmail The new email, if any.
 * @param updatedFirstName The new first name, if any.
 * @param updatedLastName The new last name, if any.
 * @author Julia Wigenstedt
 * Date: 2022-01-22
 */
public record UpdateUserInput(
        Optional<String> updatedEmail,
        Optional<String> updatedFirstName,
        Optional<String> updatedLastName
) {

    /**
     * This method can be used to get a list of all Optional fields, <br>
     * and is useful when the input needs to be validated.
     * @return A list of all (Optional) fields.
     */
    public List<Optional<?>> allFields() {
        return List.of(updatedEmail,
                updatedFirstName,
                updatedLastName);
    }
}
