
package com.amazon.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRate {

    @JsonProperty("eur")
    private Double euro;
    @JsonProperty("gbp")
    private Double britishPound;
    @JsonProperty("cad")
    private Double canadianDollar;
    @JsonProperty("jpy")
    private Double japaneseYen;
    @JsonProperty("inr")
    private Double indianRupee;
}
