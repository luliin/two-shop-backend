package io.luliin.twoshopbackend.dto;


import io.luliin.twoshopbackend.entity.AppUserEntity;
import lombok.*;

import java.sql.Timestamp;

/**
 * AppUser is the DTO that is returned when requesting information on AppUsers,
 * instead of returning the persisted {@link AppUserEntity}.
 * AppUser is the POJO equivalent of the GraphQL type AppUser.
 *
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppUser {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public ModifiedAppUser toModifiedAppUser(String message) {
        return new ModifiedAppUser(this, message);
    }
}
