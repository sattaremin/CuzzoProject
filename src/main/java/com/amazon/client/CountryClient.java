package com.amazon.client;

import com.amazon.dto.response.Country;
import com.amazon.dto.response.CountryAuthToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "COUNTRY-CLIENT",
        url = "https://www.universal-tutorial.com/api")
public interface CountryClient {

    @GetMapping("/getaccesstoken")
    CountryAuthToken getAccessToken(@RequestHeader("Accept") String accept,
                                    @RequestHeader("api-token") String apiToken,
                                    @RequestHeader("user-email") String email);

    @GetMapping("/countries")
    List<Country> getCountries(@RequestHeader("Authorization") String authorizationHeader,
                               @RequestHeader("Accept") String accept);
}
