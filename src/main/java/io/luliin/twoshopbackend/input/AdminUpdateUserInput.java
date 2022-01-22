package io.luliin.twoshopbackend.input;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-22
 */
public record AdminUpdateUserInput(
        @NotNull
        Long userId,
        Optional<String> newEmail,
        Optional<String> newUsername,
        Optional<String> newFirstName,
        Optional<String> newLastName,
        Optional<String> newPassword,
        Optional<Boolean> setAsAdmin,
        Optional<Boolean> removeAsAdmin
) {
        public List<Optional<?>> allFields() {
                return List.of(newEmail,
                        newFirstName,
                        newLastName,
                        newPassword,
                        newUsername,
                        setAsAdmin,
                        removeAsAdmin);
        }
}
