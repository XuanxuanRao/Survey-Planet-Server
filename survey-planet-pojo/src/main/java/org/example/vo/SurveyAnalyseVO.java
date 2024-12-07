package org.example.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnalyseVO {
    /**
     * 有效答卷数
     */
    private Long totalValidResponse;
    /**
     * 得分分布
     */
    private HashMap<Integer, Long> gradeCount;
    /**
     * 平均分
     */
    private Double averageGrade;
    /**
     * 最高分
     */
    private Integer highestGrade;
    /**
     * 最低分
     */
    private Integer lowestGrade;
    /**
     * 过去四个月每天的答卷数
     */
    private LinkedHashMap<LocalDate, Long> dailyResponseCount;
}
