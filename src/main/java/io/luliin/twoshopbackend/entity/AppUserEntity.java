package io.luliin.twoshopbackend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.repository.UserRoleRepository;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AppUserEntity is the Java representation of the PostgreSQL table "app_user".
 *
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user")
public class AppUserEntity implements UserDetails {

    @SequenceGenerator(
            name = "app_user_id_sequence",
            sequenceName = "app_user_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "app_user_id_sequence"
    )
    @Id

    private Long id;
    @Column(nullable = false, unique = true, length = 36)
    private String username;
    @Column(nullable = false, unique = true, length = 320)
    private String email;
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @Column(nullable = false, length = 60)
    private String firstName;
    @Column(nullable = false, length = 60)
    private String lastName;
    @Column(nullable = false)
    private Timestamp createdAt;
    @Column(nullable = false)
    private Timestamp updatedAt;
    @ManyToMany(fetch = FetchType.EAGER)
    List<UserRole> roles = new ArrayList<>();


    public AppUser toAppUser() {
        return AppUser.builder()
                .id(this.id)
                .username(this.username)
                .email(this.email)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public void addUserRole(UserRole.Role role, UserRoleRepository roleRepository) {
        UserRole currentRole = roleRepository.findByRole(role)
                .orElseThrow(() -> new IllegalArgumentException("Illegal role"));

        if(roles == null) {
            roles = new ArrayList<>();
        }
        if(!roles.contains(currentRole)) {
            roles.add(currentRole);
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.roles == null) {
            return new ArrayList<>();
        } else {
            return this.roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole().name()))
                    .collect(Collectors.toSet());
        }
    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
