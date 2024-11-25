package org.example.entity.survey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Survey {
    private Long sid;                   // 问卷id
    private Long uid;                   // 创建用户id
    private SurveyType type;            // 问卷类型
    private SurveyState state;          // 问卷状态
    private String title;               // 问卷标题
    private String description;         // 问卷描述(可选项，默认为空)
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间(上一次用户填写时间)
    private LocalDateTime openTime;     // 上一次开放时间
    private Integer fillNum;            // 填写人数

    private Integer timeLimit;          // 问卷时间限制
    private Boolean showAnswer;         // 是否允许提交后查看答案

    private Boolean allowCover;         // 是否允许覆盖填写

    /**
     * 通知模式，当问卷有新的填写时，如何通知用户
     * <p> a 2-bit mask, the first bit represents email, the second bit represents site message.
     * <p> {@link org.example.constant.NotificationModeConstant} 中定义了具体的通知方式及含义
     */
    private Integer notificationMode;

    @Override
    public int hashCode() {
        return Objects.hash(sid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Survey survey = (Survey) obj;
        return Objects.equals(sid, survey.sid);
    }
}
