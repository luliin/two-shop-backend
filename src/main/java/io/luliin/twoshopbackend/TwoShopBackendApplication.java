package io.luliin.twoshopbackend;

import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.UserRole;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@SpringBootApplication
@Slf4j
public class TwoShopBackendApplication {


    @Value("${ADMIN_USERNAME}")
    private String username;
    @Value("${ADMIN_PASSWORD}")
    private String password;
    @Value("${ADMIN_FIRSTNAME}")
    private String firstName;
    @Value("${ADMIN_LASTNAME}")
    private String lastName;
    @Value("${ADMIN_EMAIL}")
    private String email;

    public static void main(String[] args) {
        SpringApplication.run(TwoShopBackendApplication.class, args);
    }



    @Bean
    public CommandLineRunner commandLineRunner(@Autowired UserRoleRepository roleRepository,
                                               @Autowired AppUserRepository appUserRepository,
                                               @Autowired PasswordEncoder passwordEncoder) {
        return args -> {
            saveRoleIfNotPresent(roleRepository, UserRole.Role.USER, 1L);

            saveRoleIfNotPresent(roleRepository, UserRole.Role.ADMIN, 2L);

            saveRoleIfNotPresent(roleRepository, UserRole.Role.SUPER_ADMIN, 3L);

            saveAdminIfNotPresent(appUserRepository, roleRepository, passwordEncoder);

        };
    }

    private void saveRoleIfNotPresent(@Autowired UserRoleRepository roleRepository, UserRole.Role role, Long roleId) {
        if (!roleRepository.existsByRole(role)) {
            roleRepository.save(new UserRole(roleId, role));
        }
    }

    private void saveAdminIfNotPresent(AppUserRepository appUserRepository,
                                       UserRoleRepository roleRepository,
                                       PasswordEncoder passwordEncoder) {
        if(!appUserRepository.existsByUsername(username)) {
            AppUserEntity superAdmin = AppUserEntity.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .username(username)
                    .email(email)
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                    .password(passwordEncoder.encode(password))
                    .build();

            superAdmin.addUserRole(UserRole.Role.ADMIN, roleRepository);
            superAdmin.addUserRole(UserRole.Role.SUPER_ADMIN, roleRepository);
            superAdmin.addUserRole(UserRole.Role.USER, roleRepository);

            final AppUserEntity savedSuperAdmin = appUserRepository.save(superAdmin);
            log.info("SUPER_ADMIN {} has been saved to database", savedSuperAdmin.getUsername());
        } else {
            log.info("SUPER_ADMIN {} already exists", username);
        }
    }
}
