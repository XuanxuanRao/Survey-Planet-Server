package org.example.vo.survey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyVO {
    private Long sid;
    private String title;
    private String description;
    private String state;
    private String type;
    private Integer timeLimit;
}
