package io.luliin.twoshopbackend.input;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * AdminUpdateUserInput is a POJO representation of GraphQl input type with the same name.
 * @author Julia Wigenstedt
 * Date: 2022-01-22
 */
public record AdminUpdateUserInput(
        @NotNull(message = "Du måste ange användarens id för att kunna uppdatera")
        Long userId,
        Optional<String> newEmail,
        Optional<String> newUsername,
        Optional<String> newFirstName,
        Optional<String> newLastName,
        Optional<String> newPassword,
        Optional<Boolean> setAsAdmin,
        Optional<Boolean> removeAsAdmin
) {
        /**
         * This method can be used to get a list of all Optional fields, <br>
         * and is useful when the input needs to be validated.
         * @return A list of all (Optional) fields.
         */
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
