package org.example.service;

import org.example.dto.QuestionDTO;
import org.example.entity.question.Question;

import java.util.List;

public interface QuestionService {
    List<Question> getBySid(Long sid);

    /**
     * Add questions to a survey
     *
     * @param questions questions to add
     * @param sid       id of the survey to add questions to
     */
    void addQuestions(List<QuestionDTO> questions, Long sid);

    /**
     * Delete questions
     *
     * @param questions questions to delete
     */
    void deleteQuestions(List<Question> questions);

    Question getByQid(Long qid);
}
