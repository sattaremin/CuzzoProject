
package com.amazon.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrencyResponseData {

    @JsonProperty("date")
    private String date;
    @JsonProperty("usd")
    private ExchangeRate exchangeRate;
}
