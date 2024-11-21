package org.example.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.example.result.Result;
import org.example.annotation.ControllerLog;
import org.example.dto.ResponseDTO;
import org.example.entity.survey.Survey;
import org.example.entity.question.Question;
import org.example.entity.survey.SurveyState;
import org.example.exception.IllegalOperationException;
import org.example.exception.IllegalRequestException;
import org.example.exception.SurveyNotFoundException;
import org.example.service.FileService;
import org.example.service.QuestionService;
import org.example.service.ResponseService;
import org.example.service.SurveyService;
import org.example.utils.SharingCodeUtil;
import org.example.vo.ResponseVO;
import org.example.vo.survey.FilledSurveyVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@RestController
@RequestMapping("/api")
public class ResponseController {

    @Resource
    private ResponseService responseService;

    @Resource
    private SurveyService surveyService;

    @Resource
    private QuestionService questionService;

    @Resource
    private FileService fileService;

    @PostMapping("/submit")
    @ControllerLog(name = "submit", intoDB = true)
    public Result<Long> submit(@RequestBody ResponseDTO responseDTO) {
        // assure the response is successful
        return Result.success(responseService.submit(responseDTO));
    }

    @GetMapping("/fill/{code}")
    public Result<FilledSurveyVO> show(@PathVariable String code) {
        Long sid;
        try {
            sid = SharingCodeUtil.decrypt(code);
        } catch (Exception e) {
            throw new IllegalRequestException(this.getClass().getName(), "SURVEY_NOT_FOUND");
        }

        Survey survey = surveyService.getSurvey(sid);
        if (survey == null || survey.getState() == SurveyState.DELETE) {
            throw new SurveyNotFoundException("SURVEY_NOT_FOUND");
        } else if (survey.getState() == SurveyState.CLOSE) {
            throw new IllegalOperationException("SURVEY_CLOSED");
        }

        FilledSurveyVO surveyVO = FilledSurveyVO.builder()
                .questions(questionService.getBySid(sid).stream().map(Question::toFilledQuestionVO).toList())
                .type(survey.getType().getValue())
                .state(survey.getState().getValue())
                .build();
        BeanUtils.copyProperties(survey, surveyVO);

        return Result.success(surveyVO);
    }

    @GetMapping("/response/{rid}")
    public Result<ResponseVO> query(@PathVariable Long rid) {
        return Result.success(responseService.getResponseByRid(rid));
    }

    @PutMapping("/response/{rid}")
    @ControllerLog(name = "updateResponse", intoDB = true)
    public Result<Void> update(@PathVariable Long rid, @RequestBody ResponseDTO responseDTO) {
        responseService.updateResponse(rid, responseDTO.getItems(), responseDTO.getValid());
        return Result.success();
    }

    @PostMapping("/download")
    public void downloadQuestionResponses(@RequestBody List<Long> submitIds, HttpServletResponse httpServletResponse) {
        List<String> urls = responseService.getResponseItemsBySubmitIds(submitIds).stream().map(item -> item.getContent().get(0)).toList();
        fileService.downloadAndZipFiles(urls, "responses.zip", httpServletResponse);
    }

}