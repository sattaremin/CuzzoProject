package com.amazon.client;

import com.amazon.annotation.ExecutionTime;
import com.amazon.dto.response.CurrencyResponseData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "CURRENCY-CLIENT",
        url = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@2024-03-06/v1/currencies/usd.json")
public interface CurrencyClient {

    @ExecutionTime
    @GetMapping
    CurrencyResponseData getCurrency();
}