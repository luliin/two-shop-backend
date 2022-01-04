package io.luliin.twoshopbackend;

import io.luliin.twoshopbackend.entity.UserRole;
import io.luliin.twoshopbackend.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TwoShopBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TwoShopBackendApplication.class, args);
    }



    @Bean
    public CommandLineRunner commandLineRunner(@Autowired UserRoleRepository roleRepository) {
        return args -> {
            System.out.println(System.getenv("POSTGRES_PASSWORD"));
            saveRoleIfNotPresent(roleRepository, UserRole.Role.USER, 1L);

            saveRoleIfNotPresent(roleRepository, UserRole.Role.ADMIN, 2L);

            saveRoleIfNotPresent(roleRepository, UserRole.Role.SUPER_ADMIN, 3L);
        };
    }

    private void saveRoleIfNotPresent(@Autowired UserRoleRepository roleRepository, UserRole.Role role, Long roleId) {
        if (!roleRepository.existsByRole(role)) {
            roleRepository.save(new UserRole(roleId, role));
        }
    }
}
