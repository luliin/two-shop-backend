package io.luliin.twoshopbackend.repository;

import io.luliin.twoshopbackend.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
public interface AppUserRepository extends JpaRepository<AppUserEntity, Long> {
    Optional<AppUserEntity> findByUsernameOrEmail(String username, String email);
    Optional<AppUserEntity> findByUsername(String username);
    boolean existsByUsernameOrEmail(String username, String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
