package com.LearningTracker.Backend.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Assignment status.
 *
 * The DB stores the enum NAME (NOT_STARTED etc.) via @Enumerated(EnumType.STRING).
 * JSON serialization uses the display label so the frontend receives "Not Started",
 * "In Progress", "Completed" — matching the TypeScript Status type exactly.
 *
 * @JsonCreator lets Jackson deserialize BOTH the label ("Not Started") and the
 * DB name ("NOT_STARTED") so the PATCH body from the frontend works correctly.
 */
public enum AssignmentStatus {

    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String label;

    AssignmentStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static AssignmentStatus from(String value) {
        if (value == null) throw new IllegalArgumentException("Status must not be null");
        for (AssignmentStatus s : values()) {
            if (s.label.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
