package io.luliin.twoshopbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the DTO returned when a user calls the "/login" REST-endpoint
 * LoginResponse is the POJO equivalent of the GraphQL type LoginResponse.
 * @param username The authenticated user's username.
 * @param roles The authenticated user's roles.
 * @param jwtToken The generated JWT to use in subsequent API calls.
 * @author Julia Wigenstedt
 * Date: 2022-01-13
 */
public record LoginResponse(@JsonProperty("username") String username,
                            @JsonProperty("roles") List<RoleResponse> roles,
                            @JsonProperty("jwt_token") String jwtToken) {


    /**
     * A list with the DTO for role
     *
     * @param roles a collection with authenticated users GrantedAuthority
     * @return list of roles mapped to dto object
     */
    public static List<RoleResponse> convertSimpleGrantedAuthority(Collection<GrantedAuthority> roles) {
        return roles.stream()
                .map(role -> new RoleResponse(role.getAuthority()))
                .collect(Collectors.toList());
    }
}
