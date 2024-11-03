package org.example.entity.survey;

import lombok.Getter;
import org.example.exception.IllegalRequestException;

/**
 * 问卷状态，包括发布、关闭、删除三种。
 */
@Getter
public enum SurveyState {
    /**
     * 发布
     */
    OPEN,

    /**
     * 关闭
     */
    CLOSE,

    /**
     * 删除（逻辑删除，在数据库中仍然存在）
     */
    DELETE;

    private final String value = this.name().toLowerCase();

    public static SurveyState fromString(String state) {
        for (SurveyState s : SurveyState.values()) {
            if (s.name().equalsIgnoreCase(state)) {
                return s;
            }
        }
        throw new IllegalRequestException(
                SurveyState.class.getName() + ".fromString",
                "Invalid state " + state
        );
    }

    @Override
    public String toString() {
        return value;
    }
}
