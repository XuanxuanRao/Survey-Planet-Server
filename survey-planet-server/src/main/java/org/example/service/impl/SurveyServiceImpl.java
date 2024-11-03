package org.example.service.impl;

import jakarta.annotation.Resource;
import org.example.context.BaseContext;
import org.example.dto.survey.CreateSurveyDTO;
import org.example.dto.QuestionDTO;
import org.example.entity.survey.Survey;
import org.example.entity.question.Question;
import org.example.entity.response.Response;
import org.example.entity.survey.SurveyState;
import org.example.entity.survey.SurveyType;
import org.example.exception.IllegalOperationException;
import org.example.exception.SurveyNotFoundException;
import org.example.mapper.SurveyMapper;
import org.example.service.QuestionService;
import org.example.service.ResponseService;
import org.example.service.SurveyService;
import org.example.utils.SharingCodeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class SurveyServiceImpl implements SurveyService {

    @Resource
    private SurveyMapper surveyMapper;

    @Resource
    private QuestionService questionService;

    @Resource
    private ResponseService responseService;

    @Override
    public Survey getSurvey(Long sid) {
        return surveyMapper.getBySid(sid);
    }

    @Override
    public List<Survey> getSurveys(Long uid, boolean isCreated, String sortBy) {
        if (isCreated) {
            return surveyMapper.getCreatedList(uid, sortBy);
        } else {
            List<Long> sids = responseService.getResponseByUid(uid).stream().map(Response::getSid).toList();
            return surveyMapper.list(sids, sortBy);
        }
    }

    @Override
    public Survey addSurvey(CreateSurveyDTO surveyDTO) {
        Survey survey = new Survey();
        BeanUtils.copyProperties(surveyDTO, survey);
        survey.setUid(BaseContext.getCurrentId());
        survey.setType(SurveyType.fromString(surveyDTO.getType()));
        surveyMapper.insert(survey);

        return survey;
    }

    @Override
    @Transactional
    public void updateSurvey(Long sid, CreateSurveyDTO createdSurveyDTO) {
        Survey survey = getSurvey(sid);
        if (survey == null) {
            throw new SurveyNotFoundException("SURVEY_NOY_FOUND");
        } else if (survey.getOpenTime() != null) {
            throw new IllegalOperationException("CAN_NOT_MODIFY_OPENED_SURVEY");
        } else if (survey.getType() != SurveyType.fromString(createdSurveyDTO.getType())) {
            throw new IllegalOperationException("CAN_NOT_MODIFY_SURVEY_TYPE");
        }

        List<Question> originalQuestions = questionService.getBySid(sid);
        List<QuestionDTO> newQuestions = createdSurveyDTO.getQuestions();

        questionService.deleteQuestions(originalQuestions);
        questionService.addQuestions(newQuestions, sid);

        BeanUtils.copyProperties(createdSurveyDTO, survey);
        surveyMapper.update(survey);
    }

    @Override
    public String shareSurvey(Long sid) {
        Survey survey = getSurvey(sid);
        if (survey == null || survey.getState() == SurveyState.DELETE || !Objects.equals(survey.getUid(), BaseContext.getCurrentId()))
        {
            throw new SurveyNotFoundException("SURVEY_NOT_FOUND");
        }

        String code;
        try {
            code = SharingCodeUtil.encrypt(sid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (survey.getState() == SurveyState.CLOSE) {
            // 修改问卷状态
            survey.setState(SurveyState.OPEN);
            // 设置问卷上次开放时间
            survey.setOpenTime(LocalDateTime.now());
            // 更新问卷
            surveyMapper.update(survey);
        }

        return "http://59.110.163.198:3000/fill/" + code;
    }

    @Override
    public void closeSurvey(Long sid) {
        Survey survey = getSurvey(sid);
        if (survey == null || !Objects.equals(survey.getUid(), BaseContext.getCurrentId())) {
            throw new SurveyNotFoundException("SURVEY_NOT_FOUND");
        }

        survey.setState(SurveyState.CLOSE);
        surveyMapper.update(survey);
    }

    @Override
    public void modifyState(Long sid, SurveyState state) {
        Survey survey = getSurvey(sid);
        if (survey == null || !Objects.equals(survey.getUid(), BaseContext.getCurrentId())) {
            throw new SurveyNotFoundException("SURVEY_NOT_FOUND");
        }

        survey.setState(state);
        surveyMapper.update(survey);
    }

    @Override
    @Transactional
    public Integer clearSurvey() {
        // 首先获取所有已删除的问卷
        List<Survey> surveys = surveyMapper.getDeletedList();
        if (surveys.isEmpty()) {
            return 0;
        }
        // 然后删除与这些问卷关联的回答
        surveys.forEach(survey -> {
            responseService.deleteBySid(survey.getSid());
        });
        // 然后删除与这些问卷关联的问题
        surveys.forEach(survey -> {
            questionService.deleteQuestions(questionService.getBySid(survey.getSid()));
        });
        // 最后删除问卷
        return surveyMapper.delete(surveys.stream().map(Survey::getSid).toList());
    }


}
