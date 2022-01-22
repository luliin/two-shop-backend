package io.luliin.twoshopbackend.service;

import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.ShoppingList;
import io.luliin.twoshopbackend.entity.UserRole;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.ShoppingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class SharedServiceTest {

    @Mock
    AppUserRepository mockUserRepo;
    @Mock
    ShoppingListRepository mockShoppingListRepository;

    @InjectMocks
    SharedService sharedService;

    AppUserEntity user;
    UserRole userRole;
    ShoppingList shoppingList;

    @BeforeEach
    void setUp() {
        userRole = new UserRole(1L, UserRole.Role.USER);
        user = AppUserEntity.builder()
                .id(1L)
                .username("testaren")
                .password(("password"))
                .email("test@test.test")
                .firstName("Test")
                .lastName("Testsson")
                .roles(List.of(userRole))
                .createdAt(Timestamp.valueOf(LocalDateTime.now().minusDays(1)))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        shoppingList = ShoppingList.builder()
                .id(1L)
                .name("Testlistan")
                .owner(user)
                .createdAt(Timestamp.valueOf(LocalDateTime.now().minusDays(1)))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
    }

    @Test
    void getUser() {
        when(mockUserRepo.findByUsername(anyString()))
                .thenReturn(Optional.of(user));

        AppUserEntity actualUser = sharedService.getUser("testaren",
                "Test error message");

        assertEquals(user, actualUser);
        verify(mockUserRepo, times(1)).findByUsername(anyString());
    }

    @Test
    void getUserFailsWhenNoSuchUser() {
        when(mockUserRepo.findByUsername(anyString()))
                .thenReturn(Optional.empty());

        var errorMessage = "Test error message";
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> sharedService.getUser("testaren",
                "Test error message"));

        assertEquals(errorMessage, runtimeException.getMessage());
        verify(mockUserRepo, times(1)).findByUsername(anyString());
    }

    @Test
    void getUserFromUsernameOrEmail() {

        when(mockUserRepo.findByUsernameOrEmail(anyString(), anyString()))
                .thenReturn(Optional.of(user));

        AppUserEntity actualUser = sharedService.getUserFromUsernameOrEmail("testaren",
                "Test error message");

        assertEquals(user, actualUser);
        verify(mockUserRepo, times(1)).findByUsernameOrEmail(anyString(), anyString());

    }

    @Test
    void getUserFromUsernameOrEmailFailsWhenNoSuchUser() {

        when(mockUserRepo.findByUsernameOrEmail(anyString(), anyString()))
                .thenReturn(Optional.empty());

        var errorMessage = "Test error message";
        RuntimeException runtimeException =
                assertThrows(RuntimeException.class,
                        () -> sharedService.getUserFromUsernameOrEmail("testaren",
                errorMessage));

        assertEquals(errorMessage, runtimeException.getMessage());
        verify(mockUserRepo, times(1)).findByUsernameOrEmail(anyString(), anyString());


    }

    @Test
    void shoppingListById() {
        when(mockShoppingListRepository.findById(anyLong()))
                .thenReturn(Optional.of(shoppingList));

        String expectedName = "Testlistan";
        var errorMessage = "Test error message";

        var actualList = sharedService.shoppingListById(1L, errorMessage);

        assertEquals(expectedName, actualList.getName());
        assertNotEquals("Hejsan", actualList.getOwner().getUsername());
        assertEquals("testaren", actualList.getOwner().getUsername());
        verify(mockShoppingListRepository, times(1)).findById(anyLong());
    }


    @Test
    void shoppingListByIdThrowsWhenNoSuchList() {
        when(mockShoppingListRepository.findById(anyLong()))
                .thenReturn(Optional.empty());


        var errorMessage = "Test error message";

        RuntimeException runtimeException =
                assertThrows(RuntimeException.class,
                        () -> sharedService.shoppingListById(3L,
                                errorMessage));

        assertEquals(errorMessage, runtimeException.getMessage());
        verify(mockShoppingListRepository, times(1)).findById(anyLong());
    }

    @Test
    void ownedShoppingListById() {
        when(mockShoppingListRepository.findById(anyLong()))
                .thenReturn(Optional.of(shoppingList));

        String expectedName = "Testlistan";
        var errorMessage = "Test error message";

        var actualList = sharedService.ownedShoppingListById(1L, errorMessage);

        assertEquals(expectedName, actualList.getName());
        assertNotNull(actualList.getItems()); //Assert that lombok @Builder.Default is working
        assertNotEquals("Hejsan", actualList.getOwner().getUsername());
        assertEquals("testaren", actualList.getOwner().getUsername());
        verify(mockShoppingListRepository, times(1)).findById(anyLong());
    }

    @Test
    void ownedShoppingListByIdThrowsWhenNoSuchList() {

        when(mockShoppingListRepository.findById(anyLong()))
                .thenReturn(Optional.empty());


        var errorMessage = "Test error message";

        RuntimeException runtimeException =
                assertThrows(RuntimeException.class,
                        () -> sharedService.ownedShoppingListById(3L,
                                errorMessage));

        assertEquals(errorMessage, runtimeException.getMessage());
        verify(mockShoppingListRepository, times(1)).findById(anyLong());
    }
}