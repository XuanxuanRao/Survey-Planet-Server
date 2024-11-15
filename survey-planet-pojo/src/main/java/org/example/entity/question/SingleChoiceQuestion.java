package org.example.entity.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.entity.response.ResponseItem;
import org.example.vo.QuestionAnalyseVO;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 单选题
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleChoiceQuestion extends Question {
    private List<String> options;
    private List<String> answer;

    public QuestionAnalyseVO analyse(List<ResponseItem> responseItems) {
        QuestionAnalyseVO questionAnalyseVO = new QuestionAnalyseVO();
        questionAnalyseVO.setQid(this.getQid());

        long total = 0;
        LinkedHashMap<String, Long> answerCount = new LinkedHashMap<>();
        HashMap<Integer, Long> gradeCount = new HashMap<>();
        getOptions().forEach(option -> answerCount.put(option, 0L));
        for (ResponseItem responseItem : responseItems) {
            if (responseItem.getContent() == null || responseItem.getContent().isEmpty()) {
                continue;
            }
            total++;
            if (responseItem.getGrade() != null) {
                gradeCount.put(responseItem.getGrade(), gradeCount.getOrDefault(responseItem.getGrade(), 0L) + 1);
            }
            String key = responseItem.getContent().get(0);
            answerCount.put(key, answerCount.get(key) + 1);
        }
        questionAnalyseVO.setTotal(total);
        questionAnalyseVO.setAnswerCount(answerCount);
        questionAnalyseVO.setGradeCount(gradeCount);
        return questionAnalyseVO;
    }
}
