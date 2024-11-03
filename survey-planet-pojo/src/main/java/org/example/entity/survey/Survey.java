package org.example.entity.survey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
