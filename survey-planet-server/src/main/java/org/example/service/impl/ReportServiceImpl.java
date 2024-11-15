package org.example.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.example.Result.PageResult;
import org.example.entity.question.Question;
import org.example.entity.response.ResponseItem;
import org.example.exception.QuestionNotFoundException;
import org.example.service.QuestionService;
import org.example.service.ReportService;
import org.example.service.ResponseService;
import org.example.vo.QuestionAnalyseVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Resource
    private ResponseService responseService;

    @Resource
    private QuestionService questionService;

    @Override
    public QuestionAnalyseVO analyseQuestion(Long qid) {
        Question question = questionService.getByQid(qid);
        if (question == null) {
            throw new QuestionNotFoundException("QUESTION_NOT_EXIST");
        }
        List<ResponseItem> responseItems = responseService.getResponseItemsByQid(qid);
        return question.analyse(responseItems);
    }

    @Override
    public PageResult<ResponseItem> getQuestionDetail(Long qid, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<ResponseItem> pageInfo = new PageInfo<>(responseService.getResponseItemsByQid(qid));
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }
}
