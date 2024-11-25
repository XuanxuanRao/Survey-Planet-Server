package org.example.dto.survey;

import lombok.Data;
import org.example.dto.QuestionDTO;

import java.util.List;

@Data
public class CreateSurveyDTO {
    private Long uid;
    private String type;
    private String title;
    private String description;
    private List<QuestionDTO> questions;
    private Integer timeLimit;
    private Integer notificationMode;
}
