package io.luliin.twoshopbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-13
 */
public record LoginResponse(@JsonProperty("username") String username,
                            @JsonProperty("roles") List<RoleResponse> roles,
                            @JsonProperty("jwt_token") String jwtToken) {


    /**
     * A list with the DTO for role
     *
     * @param roles a collection with roles
     * @return list of roleDTO
     */
    public static List<RoleResponse> convertSimpleGrantedAuthority(Collection<GrantedAuthority> roles) {
        return roles.stream()
                .map(role -> new RoleResponse(role.getAuthority()))
                .collect(Collectors.toList());
    }
}
