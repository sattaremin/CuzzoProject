package com.amazon.enums;

public enum ProductUnit {

    LBS("Libre"),GALLON("gallon"),PCS("Pieces"),
    KG("Kilogram"),METER("Meter"),INCH("Inch"),FEET("Feet");
    private final String value;

    public String getValue() {
        return value;
    }
    ProductUnit(String value) {
        this.value = value;
    }
}
