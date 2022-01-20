package io.luliin.twoshopbackend.controller;

import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.ShoppingList;
import io.luliin.twoshopbackend.entity.UserRole;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.ShoppingListRepository;
import io.luliin.twoshopbackend.repository.UserRoleRepository;
import io.luliin.twoshopbackend.security.JWTIssuer;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureWebGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
@ContextConfiguration(initializers = ShoppingListControllerTest.TwoShopApplicationTestsContextInitializer.class)
@AutoConfigureWebGraphQlTester
@Testcontainers
@ActiveProfiles(value = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShoppingListControllerTest {

    @Container
    private static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.9.5");

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    ConnectionFactory connectionFactory;

    static RabbitAdmin rabbitAdmin;

    @Autowired
    WebGraphQlTester graphQlTester;

    @Autowired
    JWTIssuer jwtIssuer;


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
                              name: "Testlistan 2",
                              collaboratorCredential: "testaren2"
                            }
                                        
                        """;
        var mutation = """
                    mutation {
                      createShoppingList(createShoppingListInput: %s) {
                        owner {
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
                .isEqualTo(expected);

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
                .satisfies(message -> {
                    assertThat(message).isEqualTo(expectedMessage);
                });

    }

    @Test
    void deleteShoppingList() {
    }

    @Test
    void itemModified() {
    }

    @Test
    void listDeleted() {
    }


    public static class TwoShopApplicationTestsContextInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    configurableApplicationContext,
                    "spring.rabbitmq.host=" + rabbit.getContainerIpAddress(), "spring.rabbitmq.port=" + rabbit.getMappedPort(5672));

        }
    }
}