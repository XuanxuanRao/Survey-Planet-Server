package org.example.vo.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionVO {
    private Long qid;
    private String title;
    private String description;
    private String type;
    private Boolean required;
    private List<String> options;
    private Integer maxFileSize;
    private Integer score;
    private Integer timeLimit;
    private Integer memoryLimit;
    private Integer stackLimit;
}
