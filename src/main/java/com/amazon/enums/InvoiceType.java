package com.amazon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InvoiceType {

    PURCHASE("Purchase"),SALES("Sales");

    private final String Value;
}
