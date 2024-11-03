package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private String title;
    private String description;
    private String type;
    private Boolean required;
    private Integer score;

    // 单选题，多选题，填空题
    private List<String> answer;

    // 单选题和多选题
    private List<String> options;

    // 文件上传题
    private Integer maxFileSize;

    // 代码题
    private List<String> inputFileUrls;
    private List<String> outputFileUrls;
    private Integer timeLimit;
    private Integer memoryLimit;
    private Integer stackLimit;
    private Boolean isRemoveEndBlank;
    private List<String> languages;
}
