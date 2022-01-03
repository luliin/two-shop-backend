package io.luliin.twoshopbackend.input;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import io.luliin.twoshopbackend.entity.*;

/**
 * AppUserInput is the Java representation of the GraphQL input type AppUserInput,
 * which is the expected input when persisting a new {@link AppUserEntity} to the database.
 * Basic validation on the fields are provided, along with corresponding error messages in swedish.
 *
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
public record AppUserInput(
        @NotBlank(message = "Ogiltigt förnamn")
        String firstName,
        @NotBlank(message = "Ogiltigt efternamn")
        String lastName,
        @NotBlank(message = "Ogiltigt användarnamn")
        String username,
        @Email(message = "Ogiltig e-post")
        String email,
        @NotBlank(message = "Ogiltigt lösenord")
        @Length(min = 6, max = 255, message = "Lösenordet måste innehålla mellan {min} och {max} tecken")
        String password

) {
}
