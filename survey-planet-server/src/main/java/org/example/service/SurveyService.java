package org.example.service;


import org.example.dto.survey.CreateSurveyDTO;
import org.example.dto.survey.ShareSurveyDTO;
import org.example.entity.survey.Survey;
import org.example.entity.survey.SurveyState;
import org.example.result.PageResult;
import org.example.vo.survey.CreatedSurveyVO;
import org.example.vo.survey.FilledSurveyVO;

public interface SurveyService {

    Survey getSurvey(Long sid);

    PageResult<CreatedSurveyVO> getCreatedSurveys(Long uid, int pageNum, int pageSize, String sortBy);

    PageResult<FilledSurveyVO> getFilledSurveys(Long uid, int pageNum, int pageSize, String sortBy);

    Survey addSurvey(CreateSurveyDTO surveyDTO);

    Survey addSurvey(CreateSurveyDTO surveyDTO, Long uid);

    void updateSurvey(Long sid, CreateSurveyDTO createdSurveyDTO);

    /**
     * 打开问卷邀请他人填写，生成填写链接
     * @param sid 问卷id
     * @param shareSurveyDTO 发送邀请的信息
     * @return 填写链接
     */
    String shareSurvey(Long sid, ShareSurveyDTO shareSurveyDTO);

    /**
     * 关闭问卷，停止填写
     * @param sid 问卷id
     */
    void closeSurvey(Long sid);

    /**
     * 修改问卷状态
     * @param sid 问卷id
     * @param state 要设置的状态
     */
    void modifyState(Long sid, SurveyState state);

    void setNotificationMode(Long sid, Integer mode);
}
