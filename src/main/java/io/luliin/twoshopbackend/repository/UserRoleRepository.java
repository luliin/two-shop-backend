package io.luliin.twoshopbackend.repository;

import io.luliin.twoshopbackend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    boolean existsByRole(UserRole.Role role);
    Optional<UserRole> findByRole(UserRole.Role role);
}
