package io.luliin.twoshopbackend.controller;

import graphql.schema.DataFetchingEnvironment;
import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.dto.ModifiedAppUser;
import io.luliin.twoshopbackend.input.AdminUpdateUserInput;
import io.luliin.twoshopbackend.input.AppUserInput;
import io.luliin.twoshopbackend.input.UpdateUserInput;
import io.luliin.twoshopbackend.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

/**
 * AppUserController is the primary GraphQL-controller for handling
 * Queries, Mutations and Subscriptions related to AppUsers.
 * The QueryMapping and MutationMapping can be used with a REST POST
 * on path "/graphql".
 * More examples can be found on endpoint "/graphiql".
 *
 * @author Julia Wigenstedt
 * Date: 2022-01-03
 */
@Controller
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @QueryMapping
    public List<AppUser> users() {
        return appUserService.allUsers();
    }

    @QueryMapping
    public AppUser userById(@Argument Long userId) {
        return appUserService.userById(userId);
    }

    @MutationMapping
    public AppUser addUser(@Argument @Valid AppUserInput appUserInput) {
        return appUserService.addUser(appUserInput);
    }

    @MutationMapping
    public ModifiedAppUser updateUser(@Argument UpdateUserInput updateUserInput, Principal principal, DataFetchingEnvironment environment) {
        return appUserService.updateUser(updateUserInput, principal.getName(), environment);
    }

    @MutationMapping
    public ModifiedAppUser updatePassword(@Argument String oldPassword, @Argument String newPassword, Principal principal) {
        return appUserService.updatePassword(oldPassword, newPassword, principal.getName());
    }

    @MutationMapping
    public ModifiedAppUser adminUpdateUserInformation(@Argument AdminUpdateUserInput adminUpdateUserInput, DataFetchingEnvironment environment) {
        return appUserService.adminUpdateUser(adminUpdateUserInput, environment);
    }


}
