package org.example.service;

import org.example.result.PageResult;
import org.example.entity.response.ResponseItem;
import org.example.vo.QuestionAnalyseVO;
import org.example.vo.SurveyAnalyseVO;

public interface ReportService {

    QuestionAnalyseVO analyseQuestion(Long qid);

    PageResult<ResponseItem> getQuestionDetail(Long qid, Integer pageNum, Integer pageSize);

    SurveyAnalyseVO analyseSurvey(Long sid);
}
