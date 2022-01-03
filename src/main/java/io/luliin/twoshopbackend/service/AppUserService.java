package io.luliin.twoshopbackend.service;

import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.UserRole;
import io.luliin.twoshopbackend.input.AppUserInput;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final UserRoleRepository userRoleRepository;

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
                .password(appUserInput.password())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        newUser.addUserRole(UserRole.Role.USER, userRoleRepository);

        return appUserRepository.save(newUser).toAppUser();
    }

    public AppUser userById(Long userId) {
        AppUserEntity user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No such user"));

        return user.toAppUser();
    }
}
