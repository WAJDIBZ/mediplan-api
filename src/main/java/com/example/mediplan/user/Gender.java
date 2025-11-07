package com.example.mediplan.user;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum Gender {
    MALE("HOMME"),
    FEMALE("FEMME"),
    OTHER("AUTRE"),
    UNDISCLOSED("NON_PRÉCISÉ");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    @JsonCreator
    public static Gender fromString(String value) {
        for (Gender g : Gender.values()) {
            if (g.name().equalsIgnoreCase(value) ||
                    g.displayName.equalsIgnoreCase(value)) {
                return g;
            }
        }
        throw new IllegalArgumentException("Unknown gender: " + value);
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
