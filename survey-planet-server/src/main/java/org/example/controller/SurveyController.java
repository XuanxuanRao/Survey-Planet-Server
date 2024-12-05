package org.example.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.example.constant.LinkConstant;
import org.example.dto.survey.ShareSurveyDTO;
import org.example.result.PageResult;
import org.example.result.Result;
import org.example.annotation.ControllerLog;
import org.example.dto.QuestionDTO;
import org.example.dto.ResponsePageQueryDTO;
import org.example.dto.survey.CreateSurveyDTO;
import org.example.entity.response.Response;
import org.example.entity.survey.Survey;
import org.example.entity.question.Question;
import org.example.entity.survey.SurveyState;
import org.example.exception.BadQuestionException;
import org.example.exception.IllegalRequestException;
import org.example.exception.SurveyNotFoundException;
import org.example.service.QuestionService;
import org.example.service.ResponseService;
import org.example.service.SurveyService;
import org.example.utils.SharingCodeUtil;
import org.example.vo.question.CreatedQuestionVO;
import org.example.vo.survey.CreatedSurveyVO;
import org.example.context.BaseContext;
import org.example.vo.survey.SurveyVO;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/survey")
public class SurveyController {
    @Resource
    private SurveyService surveyService;
    @Resource
    private QuestionService questionService;
    @Resource
    private ResponseService responseService;

    @GetMapping("/list")
    @ControllerLog(name = "getSurveyList")
    public Result<PageResult<? extends SurveyVO>> getSurveys(
            @RequestParam String type,  // 查找创建的问卷或是填写过的问卷
            @RequestParam(defaultValue = "create_time") String sort,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize)
    {
        if (!"created".equals(type) && !"filled".equals(type)) {
            throw new IllegalRequestException("", "Invalid type " + type);
        }
        if (!"create_time".equals(sort) && !"update_time".equals(sort)) {
            throw new IllegalRequestException("", "Invalid sort " + sort);
        }

        if ("created".equals(type)) {
            return Result.success(surveyService.getCreatedSurveys(BaseContext.getCurrentId(), pageNum, pageSize, sort));
        } else {
            return Result.success(surveyService.getFilledSurveys(BaseContext.getCurrentId(), pageNum, pageSize, sort));
        }
    }

    @GetMapping("/{sid}")
    public Result<CreatedSurveyVO> getSurvey(@PathVariable Long sid) {
        // 获取问卷
        Survey survey = surveyService.getSurvey(sid);
        if (survey == null || !Objects.equals(survey.getUid(), BaseContext.getCurrentId())) {
            throw new SurveyNotFoundException("SURVEY_NOT_FOUND");
        }

        // 获取问卷的问题
        List<Question> questions = questionService.getBySid(sid);

        CreatedSurveyVO createdSurveyVO = CreatedSurveyVO.builder()
                        .questions(transfer(questions))
                        .type(survey.getType().getValue())
                        .state(survey.getState().getValue())
                        .link(LinkConstant.FILL_SURVEY + SharingCodeUtil.encrypt(sid))
                        .build();
        BeanUtils.copyProperties(survey, createdSurveyVO);

        return Result.success(createdSurveyVO);
    }

    @PostMapping("/add")
    @Transactional
    public Result<Long> createSurvey(@RequestBody CreateSurveyDTO createdSurveyDTO) {
        if (!createdSurveyDTO.getQuestions().stream().allMatch(QuestionDTO::checkFormat)) {
            throw new BadQuestionException("BAD_QUESTION");
        }

        Long sid = surveyService.addSurvey(createdSurveyDTO).getSid();
        questionService.addQuestions(createdSurveyDTO.getQuestions(), sid);

        return Result.success(sid);
    }

    @PutMapping("/{sid}")
    public Result<Void> modifySurvey(@PathVariable Long sid, @RequestBody CreateSurveyDTO createdSurveyDTO) {
        if (!createdSurveyDTO.getQuestions().stream().allMatch(QuestionDTO::checkFormat)) {
            throw new BadQuestionException("BAD_QUESTION");
        }

        surveyService.updateSurvey(sid, createdSurveyDTO);

        return Result.success();
    }

    @PutMapping("/{sid}/notify")
    public Result<Void> setNotificationMode(@PathVariable Long sid, @RequestParam Integer mode) {
        surveyService.setNotificationMode(sid, mode);
        return Result.success();
    }

    /**
     * 打开问卷邀请他人填写，生成填写链接
     * @param sid 问卷 ID
     * @return 填写链接
     */
    @PostMapping("/{sid}/share")
    public Result<String> shareSurvey(@PathVariable Long sid, @RequestBody ShareSurveyDTO shareSurveyDTO) {
        return Result.success(surveyService.shareSurvey(sid, shareSurveyDTO));
    }

    /**
     * 关闭问卷，停止填写
     * @param sid 问卷id
     */
    @PutMapping("/{sid}/close")
    public Result<Void> closeSurvey(@PathVariable Long sid) {
        surveyService.closeSurvey(sid);
        return Result.success();
    }

    /**
     * 用户角度删除问卷，只是将问卷标记为删除状态，并不从数据库中删除
     * @param sid 问卷 ID
     */
    @DeleteMapping("/{sid}")
    public Result<Void> deleteSurvey(@PathVariable Long sid) {
        surveyService.modifyState(sid, SurveyState.DELETE);
        return Result.success();
    }

    /**
     * 查询问卷的填写结果
     * @param sid 问卷 ID
     * @return 问卷的填写结果
     */
    @GetMapping("/{sid}/response/detail")
    public Result<List<Response>> getResponsesDetail(@PathVariable Long sid) {
        return Result.success(responseService.getResponseBySid(sid));
    }

    @PostMapping("/response")
    public Result<PageResult<Response>> getResponses(@RequestBody ResponsePageQueryDTO responsePageQueryDTO) {
        return Result.success(responseService.pageQuery(responsePageQueryDTO));
    }

    /**
     * 导出问卷的填写结果
     * @param sid 问卷 ID
     * @param httpServletResponse http 响应，用于携带导出文件
     */
    @SneakyThrows
    @GetMapping("/{sid}/export")
    public void exportSurvey(@PathVariable Long sid, HttpServletResponse httpServletResponse) {
        responseService.export(sid, httpServletResponse);
    }

    private List<CreatedQuestionVO> transfer(List<Question> questions) {
        return questions.stream().map(Question::toCreatedQuestionVO).collect(Collectors.toList());
    }

}
