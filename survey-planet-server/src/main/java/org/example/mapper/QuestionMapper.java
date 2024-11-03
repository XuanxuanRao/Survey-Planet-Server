package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.question.*;

import java.util.List;

@Mapper
public interface QuestionMapper {

    /**
     * Get questions by survey id
     *
     * @param sid id of the survey
     * @return list of questions
     */
    List<Question> getQuestionsBySid(Long sid);

    void insertBaseQuestions(List<Question> questions);

    void insertFileQuestion(FileQuestion question);
    void insertSingleChoiceQuestion(SingleChoiceQuestion question);
    void insertMultipleChoiceQuestion(MultipleChoiceQuestion question);
    void insertFillBlankQuestion(FillBlankQuestion question);
    void insertCodeQuestion(CodeQuestion question);

    void delete(List<Long> ids);

    void deleteFileQuestion(Long id);
    void deleteSingleChoiceQuestion(Long id);
    void deleteMultipleChoiceQuestion(Long id);
    void deleteFillBlankQuestion(Long id);
    void deleteCodeQuestion(Long id);

    Question getQuestionByQid(Long qid);
}
