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
public class QuestionAnalyseVO {
    private Long qid;
    /**
     * 有效回答数目
     */
    private Long total;
    /**
     * 回答分布
     */
    private HashMap<String, Long> answerCount;
    /**
     * 得分分布
     */
    private HashMap<Integer, Long> gradeCount;
}
