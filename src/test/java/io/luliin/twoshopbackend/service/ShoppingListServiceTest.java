package io.luliin.twoshopbackend.service;

import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.ShoppingList;
import io.luliin.twoshopbackend.entity.UserRole;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.ItemRepository;
import io.luliin.twoshopbackend.repository.ShoppingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;



/**
 * @author Julia Wigenstedt
 * Date: 2022-01-19
 */
class ShoppingListServiceTest {

    @Mock
    ShoppingListRepository mockShoppingListRepository;

    @Mock
    AppUserRepository mockAppUserRepository;

    @Mock
    ItemRepository mockItemRepository;

    @InjectMocks
    ShoppingListService shoppingListService;

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
    void getOwnedShoppingLists() {

    }

    @Test
    void getCollaboratorShoppingLists() {
    }

    @Test
    void createShoppingList() {
    }

    @Test
    void modifyShoppingListItems() {
    }

    @Test
    void publishShoppingListItems() {
    }

    @Test
    void publishDeletedList() {
    }

    @Test
    void getShoppingListPublisher() {
    }

    @Test
    void getDeletedListPublisher() {
    }

    @Test
    void addCollaborator() {
    }

    @Test
    void removeCollaborator() {
    }

    @Test
    void changeShoppingListName() {
    }

    @Test
    void clearAllItems() {
    }

    @Test
    void deleteShoppingList() {
    }
}