package org.example.entity.question;

import lombok.Getter;
import org.example.exception.IllegalRequestException;

@Getter
public enum QuestionType {
    FILL_BLANK,
    FILE,
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    CODE;

    private final String value = this.name().toLowerCase();

    public static QuestionType fromString(String type) {
        for (QuestionType t : QuestionType.values()) {
            if (t.name().equalsIgnoreCase(type)) {
                return t;
            }
        }
        throw new IllegalRequestException(
                QuestionType.class.getName() + ".fromString",
                "Invalid type " + type
        );
    }


    @Override
    public String toString() {
        return value;
    }

    public boolean equals(String type) {
        return this.value.equals(type);
    }
}
