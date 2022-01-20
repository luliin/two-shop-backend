package io.luliin.twoshopbackend.controller;


import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.UserRole;

import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.UserRoleRepository;
import io.luliin.twoshopbackend.security.JWTIssuer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
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
@AutoConfigureWebGraphQlTester
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(initializers = AppUserControllerTest.TwoShopApplicationTestsContextInitializer.class)
@Testcontainers
@ActiveProfiles(value = "test")
class AppUserControllerTest {

    @Autowired
    WebGraphQlTester graphQlTester;

    @Container
    private static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.9.5");

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ConnectionFactory connectionFactory;

    static RabbitAdmin rabbitAdmin;

    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    JWTIssuer jwtIssuer;

    String usersQuery = """
            {
                users {
                  id
                  username
                  email
                }
              }""";

    String userByIdQuery = """
                        
            {
              userById(userId: 1) {
                 id
                 username
                 firstName
              }
            }
            """;


    static AppUserEntity testUser;

    @BeforeEach
    void setUp () {
        rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.declareQueue(new Queue("forwarded-for-test"));
        rabbitAdmin.declareQueue(new Queue("deleted-for-test"));
        rabbitAdmin.declareBinding(new Binding("forwarded-for-test", Binding.DestinationType.QUEUE, "topic", "forwarded.*", null));
        rabbitAdmin.declareBinding(new Binding("deleted-for-test", Binding.DestinationType.QUEUE, "topic", "deleted.*", null));

    }

    @BeforeAll
    public static void init(@Autowired UserRoleRepository userRoleRepository,
                            @Autowired AppUserRepository appUserRepository) {

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

        testUser = appUserRepository.save(first);


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
        second.addUserRole(UserRole.Role.ADMIN, userRoleRepository);

        appUserRepository.save(second);

    }

    @AfterEach
    void tearDown() {
        rabbitAdmin.deleteQueue("deleted-for-test");
        rabbitAdmin.deleteQueue("forwarded-for-test");
    }

    @Test
    void usersPrintError() {

        this.graphQlTester.query(usersQuery)
                .execute()
                .errors()
                .satisfy(System.out::println);

    }


    @Test
    void anonymousThenUnauthorized() {
        this.graphQlTester.query(usersQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).hasSize(1);
                    assertThat(errors.get(0).getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
                });
    }


    @Test
    @WithMockUser(roles = "USER")
    void userRoleThenForbidden() {
        this.graphQlTester.query(usersQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).hasSize(1);
                    assertThat(errors.get(0).getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
                });
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRoleThenAllowed() {
        this.graphQlTester.query(usersQuery)
                .execute()
                .path("users[*].username")
                .entityList(String.class)
                .satisfies(usernames -> assertThat(usernames).contains("testaren", "testaren2"));
    }


    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void superAdminRoleThenAllowed() {
        this.graphQlTester.query(usersQuery)
                .execute()
                .path("users[*].id")
                .entityList(Long.class)
                .satisfies(usernames -> assertThat(usernames).contains(1L, 2L));
    }

    @Test
    @WithMockUser
    void userById() {
        this.graphQlTester.query(userByIdQuery)
                .execute()
                .path("userById.firstName")
                .entity(String.class)
                .satisfies(firstName -> assertThat(firstName).contains("Test"));
    }

    @Test
    void anonymousUserByIdThrowsUnauthorized() {
        this.graphQlTester.query(userByIdQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).hasSize(1);
                    assertThat(errors.get(0).getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
                });
    }

    @Test
    void addUser() {

        String expected = "Julia";

        var input = """
                {
                  firstName:"%s",
                  lastName: "Wigenstedt"
                  username: "luliin",
                  email: "luliin@test.test"
                  password: "testPassword"
                }
                """.formatted(expected);


        var addUser = """
                mutation {
                    addUser(appUserInput: %s) {
                        firstName
                    }
                }
                """.formatted(input);


        this.graphQlTester.query(addUser)
                .execute()
                .path("addUser.firstName")
                .entity(String.class)
                .isEqualTo(expected);

    }

    @Test
    void addUserTakenUsernameThrows() {

        String takenUsername = "testaren";

        var input = """
                {
                  firstName:"Julia",
                  lastName: "Wigenstedt"
                  username: "%s",
                  email: "luliin@test.test"
                  password: "testPassword"
                }
                """.formatted(takenUsername);


        var addUser = """
                mutation {
                    addUser(appUserInput: %s) {
                        firstName
                    }
                }
                """.formatted(input);

        var expected = "Username taken";

        this.graphQlTester.query(addUser)
                .execute()
                .errors()
                .satisfy(graphQLErrors -> {
                    assertThat(graphQLErrors).hasSize(1);
                    assertThat(graphQLErrors.get(0).getMessage()).isEqualTo(expected);
                });

    }


    @Test
    void validUserCanAccessApi() {

        var userToken = jwtIssuer.generateToken(testUser);

        this.graphQlTester.query(userByIdQuery)
                .httpHeaders(headers -> headers.setBearerAuth(userToken))
                .execute()
                .path("userById.firstName")
                .entity(String.class)
                .satisfies(firstName -> assertThat(firstName).contains("Test"));
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