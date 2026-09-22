package com.diecastcollector.api.enums;

public enum ModelScale {
    SCALE_1_12("1:12"),
    SCALE_1_18("1:18"),
    SCALE_1_24("1:24"),
    SCALE_1_32("1:32"),
    SCALE_1_38("1:38"),
    SCALE_1_43("1:43"),
    SCALE_1_64("1:64"),
    SCALE_1_87("1:87"),
    OTHER("Other");

    private final String description;

    ModelScale(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
