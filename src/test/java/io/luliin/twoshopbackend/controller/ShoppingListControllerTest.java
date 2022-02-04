package io.luliin.twoshopbackend.controller;

import io.luliin.twoshopbackend.AbstractContainerBaseTest;
import io.luliin.twoshopbackend.dto.DeletedListResponse;
import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.ShoppingList;
import io.luliin.twoshopbackend.entity.UserRole;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.ShoppingListRepository;
import io.luliin.twoshopbackend.repository.UserRoleRepository;
import io.luliin.twoshopbackend.security.JWTIssuer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureWebGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.graphql.web.WebGraphQlHandler;
import org.springframework.lang.NonNull;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Julia Wigenstedt
 * Date: 2022-01-20
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureWebGraphQlTester
@Testcontainers
@ActiveProfiles(value = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class ShoppingListControllerTest extends AbstractContainerBaseTest {

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    ConnectionFactory connectionFactory;
    @Autowired
    WebGraphQlTester graphQlTester;

    @Autowired
    JWTIssuer jwtIssuer;

    @Autowired
    ShoppingListRepository shoppingListRepository;

    @Autowired
    WebGraphQlHandler webGraphQlHandler;


    static AppUserEntity testUser1;
    static AppUserEntity testUser2;

    static ShoppingList testShoppingList1;
    static ShoppingList testShoppingList2;

    String userByIdQuery = """
                        
            {
              userById(userId: 1) {
                 id
                 username
                 firstName
                 ownedShoppingLists {
                    id
                    name
                 }
                 collaboratorShoppingLists {
                    id
                    name
                 }
              }
            }
            """;


    @BeforeAll
    static void init(@Autowired UserRoleRepository userRoleRepository,
                     @Autowired AppUserRepository appUserRepository,
                     @Autowired ShoppingListRepository shoppingListRepository) {

        if (!userRoleRepository.existsByRole(UserRole.Role.USER)) {
            System.out.println("Adding role " + UserRole.Role.USER.name());
            userRoleRepository.save(new UserRole(1L, UserRole.Role.USER));
        }

        if (!userRoleRepository.existsByRole(UserRole.Role.ADMIN)) {
            System.out.println("Adding role " + UserRole.Role.ADMIN.name());
            userRoleRepository.save(new UserRole(2L, UserRole.Role.ADMIN));
        }

        if (!userRoleRepository.existsByRole(UserRole.Role.SUPER_ADMIN)) {
            System.out.println("Adding role " + UserRole.Role.SUPER_ADMIN.name());
            userRoleRepository.save(new UserRole(3L, UserRole.Role.SUPER_ADMIN));
        }

        AppUserEntity first = AppUserEntity.builder()
                .id(1L)
                .username("testaren")
                .email("test@test.com")
                .password("password")
                .firstName("Test")
                .lastName("Testsson")
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        first.addUserRole(UserRole.Role.USER, userRoleRepository);
        first.addUserRole(UserRole.Role.ADMIN, userRoleRepository);
        testUser1 = appUserRepository.save(first);


        AppUserEntity second = AppUserEntity.builder()
                .id(2L)
                .username("testaren2")
                .email("test2@test.com")
                .password("password")
                .firstName("Test2")
                .lastName("Testsson2")
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        second.addUserRole(UserRole.Role.USER, userRoleRepository);


        testUser2 = appUserRepository.save(second);


        ShoppingList shoppingList = ShoppingList.builder()
                .id(1L)
                .name("Testlistan")
                .owner(testUser1)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        ShoppingList shoppingList2 = ShoppingList.builder()
                .id(2L)
                .name("Testlistan2")
                .owner(testUser2)
                .collaborator(testUser1)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        testShoppingList1 = shoppingListRepository.save(shoppingList);
        testShoppingList2 = shoppingListRepository.save(shoppingList2);


    }

    @Test
    @Order(1)
    void ownedShoppingLists() {
        var userToken = jwtIssuer.generateToken(testUser1);

        this.graphQlTester.query(userByIdQuery)
                .httpHeaders(headers -> headers.setBearerAuth(userToken))
                .execute()
                .path("userById.ownedShoppingLists[*].name")
                .entityList(String.class)
                .satisfies(names -> {
                    assertThat(names).contains("Testlistan");
                    assertThat(names).hasSize(1);
                });
    }

    @Test
    void ownedShoppingListsCannotBeViewedIfNotPermitted() {
        var userToken = jwtIssuer.generateToken(testUser2);
        this.graphQlTester.query(userByIdQuery)
                .httpHeaders(headers -> headers.setBearerAuth(userToken))
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.get(0).getPath()).contains("userById", "ownedShoppingLists");
                    assertThat(errors.get(0).getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
                })
                .path("userById.ownedShoppingLists[*].name")
                .entityList(String.class)
                .satisfies(names -> assertThat(names).hasSize(0));
    }


    @Test
    void collaboratorShoppingLists() {
        var userToken = jwtIssuer.generateToken(testUser1);
        this.graphQlTester.query(userByIdQuery)
                .httpHeaders(headers -> headers.setBearerAuth(userToken))
                .execute()
                .path("userById.collaboratorShoppingLists[*].id")
                .entityList(Long.class)
                .satisfies(ids -> {
                    assertThat(ids).contains(2L);
                    assertThat(ids).hasSize(1);
                });


    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shoppingListById() {
        var query = """
                {
                    shoppingListById(shoppingListId: 1) {
                        name
                    }
                }
                """;

        this.graphQlTester.query(query)
                .execute()
                .path("shoppingListById.name")
                .entity(String.class)
                .isEqualTo("Testlistan");


    }

    @Test
    @Order(2)
    void createShoppingList() {
        var userToken = jwtIssuer.generateToken(testUser1);

        var expected = testUser1.getUsername();


        var shoppingListInput =
                """
                            {
                              name: "Testlistan3",
                              collaboratorCredential: "testaren2"
                            }
                                        
                        """;
        var mutation = """
                    mutation {
                      createShoppingList(createShoppingListInput: %s) {
                      id
                        owner {
                          username
                        }
                        collaborator {
                            username
                        }
                      }
                    }
                """.formatted(shoppingListInput);

        this.graphQlTester.query(mutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .path("createShoppingList.owner.username")
                .entity(String.class)
                .isEqualTo(expected)
                .path("createShoppingList.collaborator.username")
                .entity(String.class)
                .isEqualTo(testUser2.getUsername());

    }

    @Test
    @Order(5)
    void modifyShoppingListItems() {
        var userToken = jwtIssuer.generateToken(testUser1);

        var expectedName = "Test cases";

        var mutation = """
                mutation {
                  modifyShoppingListItems(
                    shoppingListItemInput: {shoppingListId: 1, itemInput: {name: "%s", quantity: 2, unit: ST}}
                  ) {
                    id
                    items {
                      name
                      quantity
                      unit
                      isCompleted
                    }
                  }
                }
                """.formatted(expectedName);
        this.graphQlTester.query(mutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .path("modifyShoppingListItems.id")
                .entity(Long.class)
                .isNotEqualTo(2L)
                .isEqualTo(1L)
                .path("modifyShoppingListItems.items[0].name")
                .entity(String.class)
                .isEqualTo(expectedName);


    }

    @Test
    void modifyShoppingListItemsCanNotBeAccessedByWrongPerson() {
        var userToken = jwtIssuer.generateToken(testUser2);

        var expectedName = "Testlistan3";

        var mutation = """
                mutation {
                  modifyShoppingListItems(
                    shoppingListItemInput: {shoppingListId: 1, itemInput: {name: "%s", quantity: 2, unit: ST}}
                  ) {
                    id
                    items {
                      name
                      quantity
                      unit
                      isCompleted
                    }
                  }
                }
                """.formatted(expectedName);

        this.graphQlTester.query(mutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.get(0).getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
                    assertThat(errors.get(0).getPath()).isEqualTo(List.of("modifyShoppingListItems"));
                });

    }

    @Test
    @Order(3)
    void inviteCollaborator() {
        var userToken = jwtIssuer.generateToken(testUser1);

        var mutation = """
                    mutation {
                      inviteCollaborator(handleCollaboratorInput: {shoppingListId: 1,
                      collaboratorCredential: "%s"} ) {
                        shoppingList {
                          name
                        }
                        message
                      }
                    }
                """.formatted(testUser2.getEmail());

        var expected = "testaren2 has been added as a collaborator";


        this.graphQlTester.query(mutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isEmpty())
                .path("inviteCollaborator.shoppingList.name")
                .entity(String.class)
                .isEqualTo("Testlistan")
                .path("inviteCollaborator.message")
                .entity(String.class)
                .satisfies(message -> {
                    assertThat(message).isNotBlank();
                    assertThat(message).isEqualTo(expected);
                });
    }

    @Test
    void inviteCollaboratorThrowsErrorWithBadInput() {
        var userToken = jwtIssuer.generateToken(testUser1);

        var mutation = """
                    mutation {
                      inviteCollaborator(handleCollaboratorInput: {shoppingListId: 1,
                      collaboratorCredential: "%s"} ) {
                        shoppingList {
                          name
                        }
                        message
                      }
                    }
                """.formatted(testUser2.getUsername());

        var expected = "inviteCollaborator.handleCollaboratorInput.collaboratorCredential: " +
                "You must provide a valid email for the collaborator!";

        this.graphQlTester.query(mutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).size().isEqualTo(1);
                    assertThat(errors.get(0).getMessage()).isEqualTo(expected);
                });
    }

    @Test
    void inviteCollaboratorNonOwnerOrAdminError() {

        var userToken = jwtIssuer.generateToken(testUser2);

        var mutation = """
                    mutation {
                      inviteCollaborator(handleCollaboratorInput: {shoppingListId: 1,
                      collaboratorCredential: "%s"} ) {
                        shoppingList {
                          name
                        }
                        message
                      }
                    }
                """.formatted(testUser2.getEmail());

        this.graphQlTester.query(mutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).size().isEqualTo(1);
                    assertThat(errors.get(0).getMessage()).isEqualTo("Forbidden");
                });

    }

    @Test
    @Order(4)
    void removeCollaborator() {
        var userToken = jwtIssuer.generateToken(testUser2);

        var mutation = """
                    mutation {
                      removeCollaborator(handleCollaboratorInput: {
                        shoppingListId: 1,
                        collaboratorCredential: "%s"
                      }) {
                        shoppingList {
                          collaborator {
                            username
                          }
                        }
                        message
                      }
                    }
                """.formatted(testUser2.getEmail());

        this.graphQlTester.query(mutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .path("removeCollaborator.shoppingList.collaborator")
                .valueIsEmpty()
                .path("removeCollaborator.message")
                .entity(String.class)
                .satisfies(message -> assertThat(message).startsWith(testUser2.getEmail()));

    }

    @Test
    void changeShoppingListName() {

        var userToken = jwtIssuer.generateToken(testUser2);

        var newName = "Nya Testlistan 2";
        var oldName = testShoppingList2.getName();
        var expectedMessage = "You successfully changed the name to Nya Testlistan 2";

        var mutation =
                """
                          mutation {
                          changeShoppingListName(shoppingListId: 2, newName: "%s") {
                            shoppingList {
                              name
                            }
                            message
                          }
                        }
                        """.formatted(newName);

        this.graphQlTester.query(mutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .path("changeShoppingListName.shoppingList.name")
                .entity(String.class)
                .isNotEqualTo(oldName)
                .isEqualTo(newName)
                .path("changeShoppingListName.message")
                .entity(String.class)
                .satisfies(message -> {
                    assertThat(message).isNotEmpty();
                    assertThat(message).isEqualTo(expectedMessage);
                });
    }

    @Test
    @Order(6)
    void clearAllItems() {
        var userToken = jwtIssuer.generateToken(testUser1);
        var query = """ 
                {
                    shoppingListById(shoppingListId: 1) {
                        items {
                            id
                        }
                    }
                }
                """;

        this.graphQlTester.query(query)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .path("shoppingListById.items[*].id")
                .entityList(Long.class)
                .hasSize(1);

        var clearAllItemsMutation =
                """
                            mutation {
                              clearAllItems(shoppingListId:1) {
                                shoppingList {
                                  items {
                                    id
                                  }
                                }
                                message
                              }
                            }
                        """;

        var expectedMessage = "Testlistan är nu tom!";

        this.graphQlTester.query(clearAllItemsMutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .path("clearAllItems.shoppingList.items[*].id")
                .entityList(Long.class)
                .hasSize(0)
                .path("clearAllItems.message")
                .entity(String.class)
                .satisfies(message -> assertThat(message).isEqualTo(expectedMessage));

    }

    @Test
    @Order(8)
    void deleteShoppingListForbiddenIfNotOwnerOrAdmin() {
        var userToken = jwtIssuer.generateToken(testUser2);

        if (!shoppingListRepository.existsById(3L)) {
            ShoppingList newList = ShoppingList.builder()
                    .id(3L)
                    .name("Testlistan3")
                    .owner(testUser1)
                    .collaborator(testUser2) //Make sure the authenticated user is collaborator
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            shoppingListRepository.save(newList);
        }

        var expectedErrorMessage = "Forbidden";

        var deleteShoppingListMutation =
                """
                                mutation {
                                  deleteShoppingList(shoppingListId: 3) {
                                    message
                                    path
                                    shoppingListId
                                  }
                                }
                        """;

        this.graphQlTester.query(deleteShoppingListMutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.get(0).getMessage()).isEqualTo(expectedErrorMessage);
                    assertThat(errors.get(0).getPath()).contains("deleteShoppingList");
                });
    }

    @Test
    @Order(9)
    void deleteShoppingList() {
        var userToken = jwtIssuer.generateToken(testUser1);

        if (!shoppingListRepository.existsById(3L)) {
            ShoppingList newList = ShoppingList.builder()
                    .id(3L)
                    .name("Testlistan3")
                    .owner(testUser1) //Make sure the authenticated user is collaborator
                    .collaborator(testUser2)
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            shoppingListRepository.save(newList);
        }

        var deleteShoppingListMutation =
                """
                                mutation {
                                  deleteShoppingList(shoppingListId: 3) {
                                    message
                                    path
                                    shoppingListId
                                  }
                                }
                        """;

        var expectedMessage = testUser1.getUsername() + " tog bort Testlistan3";

        this.graphQlTester.query(deleteShoppingListMutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .path("deleteShoppingList.message")
                .entity(String.class)
                .satisfies(message -> {
                    assertThat(message).isNotEmpty();
                    assertThat(message).isEqualTo(expectedMessage);
                });


    }

    @Test
    @Order(10)
    void itemModified() throws InterruptedException {
        WebGraphQlTester subscriptionTester = WebGraphQlTester.create(webGraphQlHandler);

        var userToken = jwtIssuer.generateToken(testUser1);

        var shoppingListInput =
                """
                            {
                              name: "Testlistan 4",
                              collaboratorCredential: "testaren2"
                            }
                                        
                        """;
        var createShoppingListMutation = """
                    mutation {
                      createShoppingList(createShoppingListInput: %s) {
                        id
                      }
                    }
                """.formatted(shoppingListInput);


        Long shoppingListId = graphQlTester.query(createShoppingListMutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .path("createShoppingList.id")
                .entity(Long.class).get();

        assertThat(shoppingListId).isNotNull();


        var itemModifiedSubscription = """
                    subscription {
                        itemModified(shoppingListId: %d) {
                            id
                            items {
                                id
                                name
                            }
                        }
                    }
                """.formatted(shoppingListId);

        Flux<ShoppingList> result = subscriptionTester.query(itemModifiedSubscription)
                .executeSubscription()
                .toFlux("itemModified", ShoppingList.class);

        var firstItemName = "First";
        var secondItemName = "Second";
        var thirdItemName = "Third";


        var verify = StepVerifier.create(result)
                .consumeNextWith(shoppingList -> {
                    log.info("Items: {}", shoppingList.getItems());
                    assertThat(shoppingList.getItems()).isNotEmpty();
                    assertThat(shoppingList.getItems().get(shoppingList.getItems().size() - 1).getName()).isEqualTo(firstItemName);
                })
                .consumeNextWith(shoppingList -> {
                    log.info("Items: {}", shoppingList.getItems());
                    assertThat(shoppingList.getItems().get(shoppingList.getItems().size() - 1).getName()).isEqualTo(secondItemName);
                })
                .consumeNextWith(shoppingList -> {
                    log.info("Items: {}", shoppingList.getItems());
                    assertThat(shoppingList.getItems().get(shoppingList.getItems().size() - 1).getName()).isEqualTo(thirdItemName);
                })
                .thenCancel().verifyLater();


        addItemToShoppingList(shoppingListId, userToken, firstItemName);
        Thread.sleep(500);
        addItemToShoppingList(shoppingListId, userToken, secondItemName);
        Thread.sleep(500);
        addItemToShoppingList(shoppingListId, userToken, thirdItemName);

        verify.verify();

    }

    private void addItemToShoppingList(Long shoppingListId, String userToken, String expectedItemName) {
        var modifyItemMutation = """
                    mutation {
                                modifyShoppingListItems(shoppingListItemInput: {shoppingListId: %d, itemInput: {name: "%s", quantity: 2, unit: ST}}) {
                                    id
                                    items {
                                        id
                                        name
                                    }
                                }
                            }
                """.formatted(shoppingListId, expectedItemName);

        this.graphQlTester.query(modifyItemMutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .path("modifyShoppingListItems.id")
                .entity(Long.class)
                .isEqualTo(shoppingListId)
                .path("modifyShoppingListItems.items[*].name")
                .entityList(String.class)
                .satisfies(itemNames -> assertThat(itemNames.get(itemNames.size() - 1)).isEqualTo(expectedItemName));
    }


    @Test
    @Order(11)
    void listDeleted() {
        WebGraphQlTester subscriptionTester = WebGraphQlTester.create(webGraphQlHandler);

        var userToken = jwtIssuer.generateToken(testUser1);

        var expectedName = "Testlistan 5";

        var shoppingListInput =
                """
                            {
                              name: "%s",
                              collaboratorCredential: "testaren2"
                            }
                                        
                        """.formatted(expectedName);
        var createShoppingListMutation = """
                    mutation {
                      createShoppingList(createShoppingListInput: %s) {
                        id
                      }
                    }
                """.formatted(shoppingListInput);


        Long shoppingListId = graphQlTester.query(createShoppingListMutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .path("createShoppingList.id")
                .entity(Long.class).get();

        assertThat(shoppingListId).isNotNull();

        var listDeletedSubscription = """
                    subscription {
                      listDeleted(shoppingListId: %d) {
                          message
                        	path
                        	shoppingListId
                      }
                    }
                """.formatted(shoppingListId);

        Flux<DeletedListResponse> result = subscriptionTester.query(listDeletedSubscription)
                .executeSubscription().toFlux("listDeleted", DeletedListResponse.class);

        var expectedMessage = testUser1.getUsername() + " tog bort " + expectedName;

        var verify = StepVerifier.create(result)
                .consumeNextWith(response -> assertThat(response.message()).isEqualTo(expectedMessage))
                .thenCancel()
                .verifyLater();

        deleteShoppingList(shoppingListId, userToken, expectedMessage);

        verify.verify();

    }

    private void deleteShoppingList(Long shoppingListId, String userToken, String expectedMessage) {
        var deleteItemMutation = """
                    mutation {
                            deleteShoppingList(shoppingListId: %d) {
                                message
                                path
                                shoppingListId
                            }
                        }
                """.formatted(shoppingListId);

        this.graphQlTester.query(deleteItemMutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .path("deleteShoppingList.shoppingListId")
                .entity(Long.class)
                .isEqualTo(shoppingListId)
                .path("deleteShoppingList.message")
                .entity(String.class)
                .isEqualTo(expectedMessage);
    }

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "spring.rabbitmq.host=" + RABBIT_MQ_CONTAINER.getContainerIpAddress(), "spring.rabbitmq.port=" + RABBIT_MQ_CONTAINER.getMappedPort(5672));

    }
}