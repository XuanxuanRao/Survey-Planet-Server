package org.example.entity.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.entity.response.ResponseItem;
import org.example.vo.QuestionAnalyseVO;

import java.util.HashMap;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FillBlankQuestion extends Question {
    private List<String> answer;

    @Override
    public QuestionAnalyseVO analyse(List<ResponseItem> responseItems) {
        QuestionAnalyseVO questionAnalyseVO = new QuestionAnalyseVO();
        questionAnalyseVO.setQid(this.getQid());

        long total = 0;
        HashMap<String, Long> answerCount = new HashMap<>();
        HashMap<Integer, Long> gradeCount = new HashMap<>();
        for (ResponseItem responseItem : responseItems) {
            if (responseItem.getContent() == null || responseItem.getContent().isEmpty()) {
                continue;
            }
            total++;
            if (responseItem.getGrade() != null) {
                gradeCount.put(responseItem.getGrade(), gradeCount.getOrDefault(responseItem.getGrade(), 0L) + 1);
            }
            String key = responseItem.getContent().get(0);
            answerCount.put(key, answerCount.getOrDefault(key, 0L) + 1);
        }
        questionAnalyseVO.setTotal(total);
        questionAnalyseVO.setAnswerCount(answerCount);
        questionAnalyseVO.setGradeCount(gradeCount);
        return questionAnalyseVO;
    }
}
