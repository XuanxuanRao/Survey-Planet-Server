package org.example.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnalyseVO {
    /**
     * 有效答卷数
     */
    private Long validResponseCount;
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
}
