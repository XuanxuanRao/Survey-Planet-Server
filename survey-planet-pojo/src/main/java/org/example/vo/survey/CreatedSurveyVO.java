package org.example.vo.survey;


import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.vo.question.CreatedQuestionVO;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatedSurveyVO extends SurveyVO {
    private Long uid;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime openTime;
    private Integer fillNum;
    private List<CreatedQuestionVO> questions;
}
