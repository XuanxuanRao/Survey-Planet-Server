package org.example.entity.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.entity.response.ResponseItem;
import org.example.vo.QuestionAnalyseVO;

import java.util.HashMap;
import java.util.List;

/**
 * 多选题
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipleChoiceQuestion extends Question {
    private List<String> options;
    private List<String> answer;

    public QuestionAnalyseVO analyse(List<ResponseItem> responseItems) {
        QuestionAnalyseVO questionAnalyseVO = new QuestionAnalyseVO();
        questionAnalyseVO.setQid(this.getQid());

        long total = 0;
        HashMap<String, Long> count = new HashMap<>();
        HashMap<Integer, Long> gradeCount = new HashMap<>();
        for (ResponseItem responseItem : responseItems) {
            if (responseItem.getContent() == null || responseItem.getContent().isEmpty()) {
                continue;
            }
            total++;
            if (responseItem.getGrade() != null) {
                gradeCount.put(responseItem.getGrade(), gradeCount.getOrDefault(responseItem.getGrade(), 0L) + 1);
            }
            responseItem.getContent().forEach(option -> count.put(option, count.getOrDefault(option, 0L) + 1));
        }
        questionAnalyseVO.setTotal(total);
        questionAnalyseVO.setAnswerCount(count);
        questionAnalyseVO.setGradeCount(gradeCount);
        return questionAnalyseVO;
    }
}
