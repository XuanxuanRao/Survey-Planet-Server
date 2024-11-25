package org.example.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewSubmissionVO {
    private String surveyName;
    private String surveyLink;
    private String queryLink;
    /**
     * 新增填写数
     */
    private Integer newSubmissionNum;
    /**
     * 问卷最近提交时间
     */
    private LocalDateTime latestSubmissionTime;
}
