package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
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

    private boolean checkSingleChoice() {
        if (options == null || options.isEmpty()) {
            return false;
        }
        if (getScore() == null) {
            return answer == null || answer.isEmpty();
        } else if (getScore() > 0) {
            if (answer == null || answer.size() != 1) {
                return false;
            } else {
                return options.contains(answer.get(0));
            }
        } else {
            return false;
        }
    }

    private boolean checkMultipleChoice() {
        if (options == null || options.isEmpty()) {
            return false;
        }
        if (getScore() == null) {
            return answer == null || answer.isEmpty();
        } else if (getScore() > 0) {
            if (answer == null || answer.isEmpty()) {
                return false;
            } else {
                return new HashSet<>(options).containsAll(answer);
            }
        } else {
            return false;
        }
    }

    private boolean checkFillBlank() {
        if (getScore() == null) {
            return answer == null || answer.isEmpty();
        } else if (getScore() > 0) {
            return answer != null && answer.size() == 1;
        } else {
            return false;
        }
    }

    private boolean checkCode() {
        if (getScore() == null || getScore() <= 0) {
            return false;
        }
        if (inputFileUrls == null || inputFileUrls.isEmpty() || outputFileUrls == null || outputFileUrls.isEmpty()) {
            return false;
        }
        if (inputFileUrls.size() != outputFileUrls.size()) {
            return false;
        }
        return languages != null && !languages.isEmpty();
    }

    public boolean checkFormat() {
        return switch (type) {
            case "single_choice" -> checkSingleChoice();
            case "multiple_choice" -> checkMultipleChoice();
            case "fill_blank" -> checkFillBlank();
            case "file" -> getScore() == null || getAnswer() == null;
            case "code" -> checkCode();
            default -> false;
        };
    }
}
