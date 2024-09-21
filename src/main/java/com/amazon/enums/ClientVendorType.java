package com.amazon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter

public enum ClientVendorType implements Serializable {

    VENDOR("Vendor"), CLIENT("Client");

  private final String Value;

}
