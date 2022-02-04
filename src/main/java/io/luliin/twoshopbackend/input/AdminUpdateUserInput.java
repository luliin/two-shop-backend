package io.luliin.twoshopbackend.input;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * AdminUpdateUserInput is a POJO representation of GraphQl input type with the same name,
 * and is the expected input type used when calling adminUpdateUserInformation mutation.
 * @param userId Id of user to update.
 * @param newEmail New user email, if any.
 * @param newUsername New user username, if any.
 * @param newFirstName New user first name, if any.
 * @param newLastName New user last name, if any.
 * @param newPassword New user password, if any.
 * @param setAsAdmin If true will set provided user as Admin.
 * @param removeAsAdmin If true will remove provided user as Admin.
 *
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
