package org.example.vo.survey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.vo.question.FilledQuestionVO;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilledSurveyVO extends SurveyVO {
    private List<FilledQuestionVO> questions;
    /**
     * 最近一次提交 id
     */
    private Long rid;
    private LocalDateTime submitTime;
}
