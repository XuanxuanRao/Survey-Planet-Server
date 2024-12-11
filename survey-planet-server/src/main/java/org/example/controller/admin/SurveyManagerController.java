package org.example.controller.admin;

import jakarta.annotation.Resource;
import org.example.dto.QuestionDTO;
import org.example.dto.survey.CreateSurveyDTO;
import org.example.entity.question.Question;
import org.example.entity.survey.Survey;
import org.example.exception.IllegalOperationException;
import org.example.exception.SurveyNotFoundException;
import org.example.result.Result;
import org.example.service.QuestionService;
import org.example.service.SurveyService;
import org.example.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */

@RestController
@RequestMapping("/api/admin/survey")
public class SurveyManagerController {

    @Resource
    private UserService userService;

    @Resource
    private SurveyService surveyService;

    @Resource
    private QuestionService questionService;

    @PostMapping("/clone")
    public Result<Void> cloneSurvey(@RequestParam Long sid, @RequestParam Long uid, @RequestParam String title, @RequestParam String key) {
        if (!"wuthering_waves".equals(key)) {
            throw new IllegalOperationException("ACCESS_DENIED");
        }

        Survey survey = surveyService.getSurvey(sid);
        if (survey == null) {
            throw new SurveyNotFoundException("SURVEY_NOT_FOUND");
        }

        if (userService.getById(uid) == null) {
            throw new SurveyNotFoundException("USER_NOT_FOUND");
        }

        CreateSurveyDTO createSurveyDTO = new CreateSurveyDTO();
        createSurveyDTO.setTitle(title);
        createSurveyDTO.setType(survey.getType().toString());
        createSurveyDTO.setNotificationMode(0);
        Long resId = surveyService.addSurvey(createSurveyDTO, uid).getSid();
        List<QuestionDTO> questions = questionService.getBySid(sid).stream().map(Question::toQuestionDTO).toList();
        questionService.addQuestions(questions, resId);
        return Result.success();
    }

}
