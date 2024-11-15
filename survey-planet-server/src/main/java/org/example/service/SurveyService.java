package org.example.service;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import org.example.dto.survey.CreateSurveyDTO;
import org.example.entity.survey.Survey;
import org.example.entity.survey.SurveyState;
import org.example.vo.survey.FilledSurveyVO;

import java.util.List;

public interface SurveyService {

    Survey getSurvey(Long sid);

    List<Survey> getSurveys(Long uid, boolean isCreated, int pageNum, int pageSize, String sortBy);

    PageInfo<FilledSurveyVO> getFilledSurveys(Long uid, int pageNum, int pageSize, String sortBy);

    Survey addSurvey(CreateSurveyDTO surveyDTO);

    void updateSurvey(Long sid, CreateSurveyDTO createdSurveyDTO);

    /**
     * 打开问卷邀请他人填写，生成填写链接
     * @param sid
     * @return 填写链接
     */
    String shareSurvey(Long sid);

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

    /**
     * 删除问卷（问卷状态被标记为 {@link SurveyState}{@code .delete}）
     * @return 删除的问卷数量
     */
    Integer clearSurvey();
}
