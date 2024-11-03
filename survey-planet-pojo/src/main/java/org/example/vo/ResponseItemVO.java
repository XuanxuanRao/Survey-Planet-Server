package org.example.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.entity.judge.Judge;
import org.example.entity.response.ResponseItem;
import org.example.vo.question.FilledQuestionVO;

import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ResponseItemVO extends ResponseItem {
    private FilledQuestionVO question;

    private Judge judge;

    private List<String> answer;

}
