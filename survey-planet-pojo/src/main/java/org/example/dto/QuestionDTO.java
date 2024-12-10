package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.question.*;
import org.example.exception.IllegalRequestException;
import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


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
        if (timeLimit > 10000 || memoryLimit > 1024 || stackLimit > 512) {
            return false;
        }
        return languages != null && !languages.isEmpty();
    }

    public boolean checkFormat() {
        if (title == null || title.isEmpty() || type == null || type.isEmpty()) {
            return false;
        }
        return switch (type) {
            case "single_choice" -> checkSingleChoice();
            case "multiple_choice" -> checkMultipleChoice();
            case "fill_blank" -> checkFillBlank();
            case "file" -> getScore() == null || getAnswer() == null || (getMaxFileSize() > 0 && getMaxFileSize() <= 20);
            case "code" -> checkCode();
            default -> false;
        };
    }

    private static final Map<QuestionType, Class<? extends Question>> registry = new HashMap<>();

    static {
        registry.put(QuestionType.FILL_BLANK, FillBlankQuestion.class);
        registry.put(QuestionType.FILE, FileQuestion.class);
        registry.put(QuestionType.SINGLE_CHOICE, SingleChoiceQuestion.class);
        registry.put(QuestionType.MULTIPLE_CHOICE, MultipleChoiceQuestion.class);
        registry.put(QuestionType.CODE, CodeQuestion.class);
    }

    /**
     * 将 {@link QuestionDTO} 转换为 {@link Question} 实体类
     * @param questionDTO 要进行转化的 QuestionDTO 对象
     * @param sid 问题所属问卷 ID
     * @return {@link Question} 实体类
     */
    public Question toQuestionEntity(QuestionDTO questionDTO, Long sid) {
        QuestionType type = QuestionType.fromString(questionDTO.getType());
        Class<? extends Question> questionClass = registry.get(type);

        if (questionClass == null) {
            throw new IllegalRequestException(
                    QuestionDTO.class + ".createQuestion",
                    "Invalid question type " + questionDTO.getType()
            );
        }

        try {
            // 使用反射动态创建实例
            Question question = questionClass.getDeclaredConstructor().newInstance();
            question.setSid(sid);
            BeanUtils.copyProperties(questionDTO, question);
            question.setType(type);
            return question;
        } catch (Exception e) {
            throw new IllegalRequestException(
                    QuestionDTO.class.getName() + ".createQuestion",
                    "Error creating question of type " + questionDTO.getType()
            );
        }
    }

}
