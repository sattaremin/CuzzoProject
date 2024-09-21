package com.amazon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InvoiceStatus {
    AWAITING_APPROVAL("Awaiting Approval"),APPROVED("Approved");

    private final String Value;
}
