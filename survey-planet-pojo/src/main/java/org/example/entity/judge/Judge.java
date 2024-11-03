package org.example.entity.judge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Judge {
    private Long submitId;

    /**
     * 所属题目id
     */
    private Long qid;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 使用的语言
     */
    private String language;

    /**
     * 提交的代码
     */
    private String codeContent;

    /**
     * 判题结果
     */
    private Integer status;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 错误提醒，比如 COMPILE ERROR
     */
    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 每个测试点的详细结果
     */
    List<CaseJudgeResult> caseJudgeResults;
}
