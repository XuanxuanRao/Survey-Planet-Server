package org.example.vo;

import com.kennycason.kumo.WordFrequency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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
    private LinkedHashMap<String, Long> answerCount;
    /**
     * 得分分布
     */
    private HashMap<Integer, Long> gradeCount;
    /**
     * 词云
     */
    private List<WordFrequency> wordCloud;
}
