package org.example.service.factory;

import org.example.dto.QuestionDTO;
import org.example.entity.question.*;
import org.example.entity.question.QuestionType;
import org.example.exception.IllegalRequestException;
import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.Map;

public class QuestionFactory {
    // 使用 Map 存储 QuestionType 和 Question 类的对应关系
    private static final Map<QuestionType, Class<? extends Question>> registry = new HashMap<>();

    static {
        registry.put(QuestionType.FILL_BLANK, FillBlankQuestion.class);
        registry.put(QuestionType.FILE, FileQuestion.class);
        registry.put(QuestionType.SINGLE_CHOICE, SingleChoiceQuestion.class);
        registry.put(QuestionType.MULTIPLE_CHOICE, MultipleChoiceQuestion.class);
        registry.put(QuestionType.CODE, CodeQuestion.class);
    }

    public static Question createQuestion(QuestionDTO questionDTO, Long sid) {
        QuestionType type = QuestionType.fromString(questionDTO.getType());
        Class<? extends Question> questionClass = registry.get(type);

        if (questionClass == null) {
            throw new IllegalRequestException(
                    QuestionFactory.class.getName() + ".createQuestion",
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
                    QuestionFactory.class.getName() + ".createQuestion",
                    "Error creating question of type " + questionDTO.getType()
            );
        }
    }


}
