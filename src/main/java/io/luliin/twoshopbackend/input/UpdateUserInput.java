package io.luliin.twoshopbackend.input;

import java.util.List;
import java.util.Optional;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-22
 */
public record UpdateUserInput(
        Optional<String> updatedEmail,
        Optional<String> updatedFirstName,
        Optional<String> updatedLastName
) {

    public List<Optional<?>> allFields() {
        return List.of(updatedEmail,
                updatedFirstName,
                updatedLastName);
    }
}
