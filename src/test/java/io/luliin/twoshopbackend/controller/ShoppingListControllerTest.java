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
class ShoppingListControllerTest {

    @Container
    private static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.9.5");

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired ConnectionFactory connectionFactory;

    static RabbitAdmin rabbitAdmin;

    @Autowired
    WebGraphQlTester graphQlTester;

    @Autowired
    JWTIssuer jwtIssuer;



    static AppUserEntity testUser1;
    static AppUserEntity testUser2;

    static ShoppingList testShoppingList;

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
              }
            }
            """;

    @BeforeEach
    void setUp () {
        rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.declareQueue(new Queue("only-for-test"));
        rabbitAdmin.declareBinding(new Binding("only-for-test", Binding.DestinationType.QUEUE, "topic", "forwarded.*", null));
        rabbitAdmin.declareBinding(new Binding("only-for-test", Binding.DestinationType.QUEUE, "topic", "deleted.*", null));

    }

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


        testUser2 =appUserRepository.save(second);


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

        testShoppingList = shoppingListRepository.save(shoppingList);
        shoppingListRepository.save(shoppingList2);


    }

    @AfterEach
   void tearDown() {
        rabbitAdmin.deleteQueue("only-for-test");
    }

    @Test
    void ownedShoppingLists() {

        //
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
    }

    @Test
    @WithMockUser(username = "testaren", roles = "ADMIN")
    void shoppingListById() {
        var query =  """
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
    void createShoppingList() {
    }

    @Test
    void modifyShoppingListItems() {
    }

    @Test
    void inviteCollaborator() {
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