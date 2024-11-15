package org.example.service;

import org.example.Result.PageResult;
import org.example.entity.response.ResponseItem;
import org.example.vo.QuestionAnalyseVO;

public interface ReportService {

    QuestionAnalyseVO analyseQuestion(Long qid);

    PageResult<ResponseItem> getQuestionDetail(Long qid, Integer pageNum, Integer pageSize);
}
