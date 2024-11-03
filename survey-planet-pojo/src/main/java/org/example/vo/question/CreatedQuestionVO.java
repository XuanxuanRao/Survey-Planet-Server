package org.example.vo.question;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatedQuestionVO extends QuestionVO {
    private List<String> answer;
    private List<String> inputFileUrls;
    private List<String> outputFileUrls;
    private Boolean isRemoveEndBlank;
    private List<String> languages;
}
