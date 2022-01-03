package io.luliin.twoshopbackend.controller;

import io.luliin.twoshopbackend.dto.AppUser;
import io.luliin.twoshopbackend.input.AppUserInput;
import io.luliin.twoshopbackend.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
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



}
