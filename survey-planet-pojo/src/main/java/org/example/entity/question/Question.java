package org.example.entity.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.response.ResponseItem;
import org.example.vo.QuestionAnalyseVO;
import org.example.vo.question.CreatedQuestionVO;
import org.example.vo.question.FilledQuestionVO;
import org.springframework.beans.BeanUtils;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private Long qid;
    private Long sid;
    private String title;
    private String description;
    private QuestionType type;
    private Boolean required;
    private Integer score;

    public CreatedQuestionVO toCreatedQuestionVO() {
        CreatedQuestionVO questionVO = new CreatedQuestionVO();
        questionVO.setType(this.getType().getValue());
        BeanUtils.copyProperties(this, questionVO);
        return questionVO;
    }

    public FilledQuestionVO toFilledQuestionVO() {
        FilledQuestionVO questionVO = new FilledQuestionVO();
        questionVO.setType(this.getType().getValue());
        BeanUtils.copyProperties(this, questionVO);
        return questionVO;
    }

    public QuestionAnalyseVO analyse(List<ResponseItem> responseItems) {
        return null;
    }

}
