package org.example.entity.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.dto.QuestionDTO;
import org.example.entity.response.ResponseItem;
import org.example.vo.QuestionAnalyseVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        LinkedHashMap<String, Long> count = new LinkedHashMap<>();
        HashMap<Integer, Long> gradeCount = new HashMap<>();
        options.forEach(option -> count.put(option, 0L));
        for (ResponseItem responseItem : responseItems) {
            if (responseItem.getContent() == null || responseItem.getContent().isEmpty()) {
                continue;
            }
            total++;
            if (responseItem.getGrade() != null) {
                gradeCount.put(responseItem.getGrade(), gradeCount.getOrDefault(responseItem.getGrade(), 0L) + 1);
            }
            responseItem.getContent().forEach(option -> count.put(option, count.get(option) + 1));
        }
        questionAnalyseVO.setTotal(total);
        questionAnalyseVO.setAnswerCount(count);
        questionAnalyseVO.setGradeCount(gradeCount);
        return questionAnalyseVO;
    }

    @Override
    public QuestionDTO toQuestionDTO() {
        var res = super.toQuestionDTO();
        if (options != null)    res.setOptions(new ArrayList<>(options));
        if (answer != null)     res.setAnswer(new ArrayList<>(answer));
        return res;
    }
}
