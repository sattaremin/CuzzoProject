package com.amazon.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CountryAuthToken {

    @JsonProperty("auth_token")
    private String authToken;
}
