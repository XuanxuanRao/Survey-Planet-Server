package org.example.entity.question;

import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.dto.QuestionDTO;
import org.example.entity.response.ResponseItem;
import org.example.vo.QuestionAnalyseVO;

import java.util.ArrayList;
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
        HashMap<Integer, Long> gradeCount = new HashMap<>();
        FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
        // 设置分词返回数量(频率最高的50个词)
        frequencyAnalyzer.setWordFrequenciesToReturn(50);
        // 最小分词长度
        frequencyAnalyzer.setMinWordLength(2);
        // 引入中文解析器
        frequencyAnalyzer.setWordTokenizer(new ChineseWordTokenizer());
        List<String> texts = new ArrayList<>();
        for (ResponseItem responseItem : responseItems) {
            if (responseItem.getContent() == null || responseItem.getContent().isEmpty()) {
                continue;
            }
            total++;
            texts.add(responseItem.getContent().get(0));
            if (responseItem.getGrade() != null) {
                gradeCount.put(responseItem.getGrade(), gradeCount.getOrDefault(responseItem.getGrade(), 0L) + 1);
            }
        }
        final List<WordFrequency> wordFrequency = frequencyAnalyzer.load(texts);
        questionAnalyseVO.setTotal(total);
        questionAnalyseVO.setGradeCount(gradeCount);
        questionAnalyseVO.setWordCloud(wordFrequency);
        return questionAnalyseVO;
    }

    @Override
    public QuestionDTO toQuestionDTO() {
        var res = super.toQuestionDTO();
        res.setAnswer(new ArrayList<>(answer));
        return res;
    }
}
