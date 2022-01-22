package io.luliin.twoshopbackend.service;

import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.dto.ModifiedAppUser;
import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.UserRole;
import io.luliin.twoshopbackend.exception.CustomValidationException;
import io.luliin.twoshopbackend.exception.InvalidEmailException;
import io.luliin.twoshopbackend.input.AdminUpdateUserInput;
import io.luliin.twoshopbackend.input.AppUserInput;
import io.luliin.twoshopbackend.input.UpdateUserInput;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class AppUserService extends DataFetcherExceptionResolverAdapter {

    private final AppUserRepository appUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_SUPER_ADMIN')")
    public List<AppUser> allUsers() {
        return appUserRepository.findAll().stream()
                .map(AppUserEntity::toAppUser)
                .collect(Collectors.toList());
    }

    public AppUser addUser(AppUserInput appUserInput) {
        if(appUserRepository.existsByUsername(appUserInput.username())) {
            throw new IllegalArgumentException("Username taken");
        }
        if(appUserRepository.existsByEmail(appUserInput.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        AppUserEntity newUser = AppUserEntity.builder()
                .firstName(appUserInput.firstName())
                .lastName(appUserInput.lastName())
                .username(appUserInput.username())
                .email(appUserInput.email())
                .password(passwordEncoder.encode(appUserInput.password()))
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        newUser.addUserRole(UserRole.Role.USER, userRoleRepository);

        return appUserRepository.save(newUser).toAppUser();
    }

    @PreAuthorize("isAuthenticated()")
    public AppUser userById(Long userId) {
        AppUserEntity user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No such user"));

        return user.toAppUser();
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ModifiedAppUser updateUser(UpdateUserInput updateUserInput, String username, DataFetchingEnvironment environment) {
        AppUserEntity currentUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Could not fetch current user's information."));

        if(updateUserInput.allFields().stream()
        .allMatch(Optional::isEmpty)) {
            throw new CustomValidationException("Du har inte angett någon ny information.");
        }

        updateUserInput.updatedEmail().ifPresentOrElse(email -> currentUser.setEmail(validatedEmail(email, environment)),
                () -> log.info("No email provided"));
        updateUserInput.updatedFirstName().ifPresentOrElse(firstName -> currentUser.setFirstName(validatedName(firstName, "Förnamn")),
                () -> log.info("No first name provided"));
        updateUserInput.updatedLastName().ifPresentOrElse(lastName -> currentUser.setLastName(validatedName(lastName, "Efternamn")),
                () -> log.info("No last name provided"));
        currentUser.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return appUserRepository.save(currentUser).toAppUser().toModifiedAppUser("Din information har uppdaterats!");
    }

    @PreAuthorize("isAuthenticated()")
    public ModifiedAppUser updatePassword(String oldPassword, String newPassword, String username) {
        AppUserEntity currentUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Could not fetch current user's information."));

        if(!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new CustomValidationException("Fel lösenord. Försök igen!");
        }

        currentUser.setPassword(passwordEncoder.encode(validatedPassword(newPassword)));
        currentUser.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        return appUserRepository.save(currentUser).toAppUser()
                .toModifiedAppUser("Ditt lösenord har uppdaterats!");

    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ModifiedAppUser adminUpdateUser(@Valid AdminUpdateUserInput adminUpdateUserInput, DataFetchingEnvironment environment) {
        AppUserEntity userToUpdate = appUserRepository.findById(adminUpdateUserInput.userId())
                .orElseThrow(() -> new IllegalArgumentException("No such user"));
        if(adminUpdateUserInput.allFields().stream()
                .allMatch(Optional::isEmpty)) {
            throw new CustomValidationException("No fields to update have been provided");
        }

        adminUpdateUserInput.newEmail().ifPresentOrElse(email -> userToUpdate.setEmail(validatedEmail(email, environment)),
                () -> log.info("No email provided"));
        adminUpdateUserInput.newFirstName().ifPresentOrElse(firstName -> userToUpdate.setFirstName(validatedName(firstName, "Förnamn")),
                () -> log.info("No first name provided"));
        adminUpdateUserInput.newLastName().ifPresentOrElse(lastName -> userToUpdate.setLastName(validatedName(lastName, "Efternamn")),
                () -> log.info("No last name provided"));
        adminUpdateUserInput.newUsername().ifPresentOrElse(username -> userToUpdate.setUsername(validatedName(username, "Användarnamn")),
                () -> log.info("No username provided"));
        adminUpdateUserInput.newPassword().ifPresentOrElse(password -> userToUpdate.setPassword(validatedPassword(password)),
                () -> log.info("No password provided"));

        adminUpdateUserInput.setAsAdmin().ifPresentOrElse(bool -> {
            log.info("Setting {} to admin", userToUpdate.getUsername());
            if(bool) userToUpdate.addUserRole(UserRole.Role.ADMIN, userRoleRepository);
            else log.info("Do not set as admin");
                },
                () -> log.info("No setAsAdmin provided"));
        adminUpdateUserInput.removeAsAdmin().ifPresentOrElse(bool -> {
                    log.info("Removing {} as admin", userToUpdate.getUsername());
                    if(bool) userToUpdate.removeUserRole(UserRole.Role.ADMIN, userRoleRepository);
                    else log.info("Do not remove as admin");
                },
                () -> log.info("No removeAsAdmin provided"));

        userToUpdate.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        final AppUser appUser = appUserRepository.save(userToUpdate).toAppUser();
        return appUser.toModifiedAppUser(appUser.getUsername() + " has been updated successfully!");


    }
    public String validatedEmail(String email, DataFetchingEnvironment environment) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-åäö]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        if(!pattern.matcher(email).matches()) throw new InvalidEmailException("Ogiltig e-post");
        return email;
    }

    public String validatedName(String name, String typeName) {
        if(name == null || name.trim().length()==0) throw new CustomValidationException(typeName + "et får inte vara blankt");
        return name;
    }

    public String validatedPassword(String password) {
        if(password == null || password.trim().length()==0) throw new CustomValidationException("Lösenordet får inte vara blankt");
        if(password.length()<6 || password.length()>255) throw new CustomValidationException("Lösenordet måste innehålla mellan 6 och 255 tecken");
        return password;
    }
}
