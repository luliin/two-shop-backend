package io.luliin.twoshopbackend.controller;


import io.luliin.twoshopbackend.AbstractContainerBaseTest;
import io.luliin.twoshopbackend.dto.ModifiedAppUser;
import io.luliin.twoshopbackend.entity.AppUserEntity;
import io.luliin.twoshopbackend.entity.UserRole;
import io.luliin.twoshopbackend.repository.AppUserRepository;
import io.luliin.twoshopbackend.repository.UserRoleRepository;
import io.luliin.twoshopbackend.security.JWTIssuer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureWebGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Julia Wigenstedt
 * Date: 2022-01-20
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebGraphQlTester
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@Testcontainers
@ActiveProfiles(value = "test")
class AppUserControllerTest extends AbstractContainerBaseTest {

    @Autowired
    WebGraphQlTester graphQlTester;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ConnectionFactory connectionFactory;

    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JWTIssuer jwtIssuer;

    String usersQuery = """
            {
                users {
                  id
                  username
                  email
                  lastName
                  createdAt
                  updatedAt
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

    @BeforeAll
    public static void init(@Autowired UserRoleRepository userRoleRepository,
                            @Autowired AppUserRepository appUserRepository,
                            @Autowired PasswordEncoder passwordEncoder) {

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
                .password(passwordEncoder.encode("password"))
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
                .password(passwordEncoder.encode("password"))
                .firstName("Test2")
                .lastName("Testsson2")
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        second.addUserRole(UserRole.Role.USER, userRoleRepository);
        second.addUserRole(UserRole.Role.ADMIN, userRoleRepository);

        appUserRepository.save(second);

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


        var addUser =
                """
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


    @Test
    void updateUser() {

        var userToken = jwtIssuer.generateToken(testUser);
        var expectedFirstName = "Test-Arne";
        var unexpectedFirstName = testUser.getFirstName();
        var expectedMessage = "Din information har uppdaterats!";
        var updateUserMutation = """
                    mutation {
                        updateUser(updateUserInput:{
                        updatedFirstName: "%s"} ) {
                            appUser {
                                id
                                firstName
                                lastName
                            }
                            message
                        }
                    }
                """.formatted(expectedFirstName);

        this.graphQlTester.query(updateUserMutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .path("updateUser")
                .entity(ModifiedAppUser.class)
                .satisfies(modifiedAppUser -> {
                    var appUser = modifiedAppUser.appUser();
                    assertThat(appUser.getId()).isEqualTo(testUser.getId());
                    assertThat(appUser.getFirstName()).isNotEqualTo(unexpectedFirstName);
                    assertThat(appUser.getFirstName()).isEqualTo(expectedFirstName);
                    assertThat(appUser.getLastName()).isEqualTo(testUser.getLastName());
                    assertThat(appUser.getUsername()).isNull();
                    assertThat(modifiedAppUser.message()).isEqualTo(expectedMessage);
                });
    }

    @Test
    void updateUserNoInformationValidationError() {

        var userToken = jwtIssuer.generateToken(testUser);
        var updateUserMutation = """
                    mutation {
                        updateUser(updateUserInput:{} ) {
                            message
                        }
                    }
                """;

        var expectedErrorMessage = "Du har inte angett någon ny information.";
        this.graphQlTester.query(updateUserMutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.get(0).getErrorType()).isEqualTo(graphql.ErrorType.ValidationError);
                    assertThat(errors.get(0).getMessage()).isEqualTo(expectedErrorMessage);
                });
    }

    @Test
    void updateUserEmailValidationError() {

        var userToken = jwtIssuer.generateToken(testUser);
        var updateUserMutation = """
                    mutation {
                        updateUser(updateUserInput:{
                        updatedEmail: "email"} ) {
                            message
                        }
                    }
                """;

        var expectedErrorMessage = "Ogiltig e-post";
        this.graphQlTester.query(updateUserMutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.get(0).getErrorType()).isEqualTo(graphql.ErrorType.ValidationError);
                    assertThat(errors.get(0).getMessage()).isEqualTo(expectedErrorMessage);
                    assertThat(errors.get(0).getPath()).contains("updateUser");
                });
    }

    @Test
    void unauthenticatedCannotUpdateUser() {

        var updateUserMutation = """
                    mutation {
                        updateUser(updateUserInput:{
                        updatedEmail: "email"} ) {
                            message
                        }
                    }
                """;

        var expectedErrorMessage = "Unauthorized";
        this.graphQlTester.query(updateUserMutation)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.get(0).getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
                    assertThat(errors.get(0).getMessage()).isEqualTo(expectedErrorMessage);
                    assertThat(errors.get(0).getPath()).contains("updateUser");
                });
    }

    @Test
    void updatePassword() {
        var userToken = jwtIssuer.generateToken(testUser);

        var newPassword = "newPassword";

        var expectedMessage = "Ditt lösenord har uppdaterats!";
        var updatePasswordMutation = """
                    mutation {
                        updatePassword(oldPassword: "password", newPassword: "%s" ) {
                            message
                        }
                    }
                """.formatted(newPassword);

        this.graphQlTester.query(updatePasswordMutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .path("updatePassword.message")
                .entity(String.class)
                .isEqualTo(expectedMessage);

        var updatedUser = appUserRepository.findById(testUser.getId())
                .orElseThrow(() -> new RuntimeException("An unexpected error occurred"));

        assertTrue(this.passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @Test
    void updatePasswordFailsIfOldPasswordIsWrong() {
        var userToken = jwtIssuer.generateToken(testUser);

        var newPassword = "newPassword";

        var expectedMessage = "Fel lösenord. Försök igen!";
        var updatePasswordMutation = """
                    mutation {
                        updatePassword(oldPassword: "invalid_password", newPassword: "%s" ) {
                            message
                        }
                    }
                """.formatted(newPassword);

        this.graphQlTester.query(updatePasswordMutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).size().isEqualTo(1);
                    assertThat(errors.get(0).getMessage()).isEqualTo(expectedMessage);
                    assertThat(errors.get(0).getErrorType()).isEqualTo(graphql.ErrorType.ValidationError);
                });

        var updatedUser = appUserRepository.findById(testUser.getId())
                .orElseThrow(() -> new RuntimeException("An unexpected error occurred"));

        assertFalse(this.passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @Test
    void adminUpdateUserInformationNonAdminForbidden() {
        var userToken = jwtIssuer.generateToken(testUser);

        var adminUpdateUserMutation = """
                    mutation {
                        adminUpdateUserInformation(adminUpdateUserInput: {
                            userId: 2
                        } ) {
                            message
                        }
                    }
                """;

        this.graphQlTester.query(adminUpdateUserMutation)
                .httpHeaders(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).size().isEqualTo(1);
                    assertThat(errors.get(0).getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
                });


    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminUpdateUserInformationValidationError() {
        var adminUpdateUserMutation = """
                    mutation {
                        adminUpdateUserInformation(adminUpdateUserInput: {
                            userId: 2
                        } ) {
                            message
                        }
                    }
                """;

        var expectedMessage = "No fields to update have been provided";
        this.graphQlTester.query(adminUpdateUserMutation)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).size().isEqualTo(1);
                    assertThat(errors.get(0).getErrorType()).isEqualTo(graphql.ErrorType.ValidationError);
                    assertThat(errors.get(0).getMessage()).isEqualTo(expectedMessage);
                });

    }

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "spring.rabbitmq.host=" + RABBIT_MQ_CONTAINER.getContainerIpAddress(), "spring.rabbitmq.port=" + RABBIT_MQ_CONTAINER.getMappedPort(5672));

    }


}