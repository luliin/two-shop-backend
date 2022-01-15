package io.luliin.twoshopbackend.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-13
 */
@Getter
public class RoleResponse {
    private final String name;

    @JsonCreator
    public RoleResponse(@JsonProperty("name") String name) {
        this.name = name;
    }
}
