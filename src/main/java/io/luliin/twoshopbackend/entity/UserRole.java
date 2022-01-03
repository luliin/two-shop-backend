package io.luliin.twoshopbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * UserRole is the Java representation of the PostgreSQL table "user_role".
 * This entity is not exposed to the outside.
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_role")
public class UserRole {

    @SequenceGenerator(
            name = "user_role_id_sequence",
            sequenceName = "user_role_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_role_id_sequence"
    )
    @Id
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private Role role;

    public enum Role {
        USER,
        ADMIN,
        SUPER_ADMIN
    }
}
