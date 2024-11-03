package org.example.entity.survey;

import lombok.Getter;
import org.example.exception.IllegalRequestException;

@Getter
public enum SurveyType {
    NORMAL,
    EXAM;

    private final String value = this.name().toLowerCase();

    public static SurveyType fromString(String type) {
        for (SurveyType t : SurveyType.values()) {
            if (t.name().equalsIgnoreCase(type)) {
                return t;
            }
        }
        throw new IllegalRequestException(
                SurveyType.class.getName() + ".fromString()",
                "unknown survey type: " + type
        );
    }

    @Override
    public String toString() {
        return value;
    }
}
