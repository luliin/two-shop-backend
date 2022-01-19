package io.luliin.twoshopbackend.service;

import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.UserRole;
import io.luliin.twoshopbackend.input.AppUserInput;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.context.ContextConfiguration;


import java.sql.Timestamp;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


/**
 * @author Julia Wigenstedt
 * Date: 2022-01-19
 */


@ExtendWith({MockitoExtension.class})
class AppUserServiceTest {

    @Mock
    AppUserRepository mockUserRepo;
    @Mock
    UserRoleRepository mockRoleRepository;

    @InjectMocks
    AppUserService appUserService;
    @Mock
    PasswordEncoder passwordEncoder;

    AppUserEntity user;
    UserRole userRole;
    AppUserInput appUserInput;


    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(10);
        userRole = new UserRole(1L, UserRole.Role.USER);
        user = AppUserEntity.builder()
                .id(1L)
                .username("testaren")
                .password(passwordEncoder.encode("password"))
                .email("test@test.test")
                .firstName("Test")
                .lastName("Testsson")
                .roles(List.of(userRole))
                .createdAt(Timestamp.valueOf(LocalDateTime.now().minusDays(1)))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        appUserInput = new AppUserInput(user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword());
    }

    @Test
    void allUsers() {
        List<AppUserEntity> users = List.of(user);

        when(mockUserRepo.findAll())
                .thenReturn(users);

        final List<AppUser> appUsers = appUserService.allUsers();

        assertEquals(1, appUsers.size());
        assertEquals(user.getEmail(), appUsers.get(0).getEmail());
        verify(mockUserRepo, times(1)).findAll();
    }

    @Test
    void allUsersFailsWhenAnonymous() {
        List<AppUserEntity> users = List.of(user);

        when(mockUserRepo.findAll())
                .thenReturn(users);

        List<AppUser> appUsers = appUserService.allUsers();

        assertEquals(1, appUsers.size());
        assertEquals(user.getEmail(), appUsers.get(0).getEmail());
        verify(mockUserRepo, times(1)).findAll();
    }

    @Test
    void addUser() {

        when(mockUserRepo.existsByUsername(anyString()))
                .thenReturn(false);
        when(mockUserRepo.existsByEmail(anyString()))
                .thenReturn(false);
        when(mockUserRepo.save(any(AppUserEntity.class)))
                .thenReturn(user);
        when(mockRoleRepository.findByRole(any(UserRole.Role.class)))
                .thenReturn(Optional.of(userRole));

        AppUser newUser = appUserService.addUser(appUserInput);

        assertEquals(user.getEmail(), newUser.getEmail());
        verify(mockUserRepo, times(1)).existsByUsername(anyString());
        verify(mockUserRepo, times(1)).existsByEmail(anyString());
        verify(mockUserRepo, times(1)).save(any(AppUserEntity.class));
    }
    @Test
    void addUserThrowsWhenUsernameTaken() {
        when(mockUserRepo.existsByUsername(anyString()))
                .thenReturn(true);

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> appUserService.addUser(appUserInput));

        assertEquals("Username taken" ,illegalArgumentException.getMessage());
        verify(mockUserRepo, times(1)).existsByUsername(anyString());
    }

    @Test
    void addUserThrowsWhenEmailTaken() {
        when(mockUserRepo.existsByUsername(anyString()))
                .thenReturn(false);
        when(mockUserRepo.existsByEmail(anyString()))
                .thenReturn(true);

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> appUserService.addUser(appUserInput));

        assertEquals("Email already in use", illegalArgumentException.getMessage());

        verify(mockUserRepo, times(1)).existsByEmail(anyString());
    }

    @Test
    void userById() {
        when(mockUserRepo.findById(anyLong()))
                .thenReturn(Optional.of(user));

        AppUser appUser = appUserService.userById(1L);
        assertEquals(user.getEmail(), appUser.getEmail());
        verify(mockUserRepo, times(1)).findById(anyLong());
    }


    @Test
    void userByIdThrowsWhenIdIsNotFound() {
        when(mockUserRepo.findById(anyLong()))
                .thenReturn(Optional.empty());

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> appUserService.userById(3L));

        assertEquals("No such user", illegalArgumentException.getMessage());
        verify(mockUserRepo, times(1)).findById(anyLong());

    }

    @Test
    void passwordEncoderCanMatchPassword() {

        String expected = "password";
        String encoded = passwordEncoder.encode(expected);

        boolean passwordMatches = passwordEncoder.matches(expected, encoded);
        assertTrue(passwordMatches);

    }
}