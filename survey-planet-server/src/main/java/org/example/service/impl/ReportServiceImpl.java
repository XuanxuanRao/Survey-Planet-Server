package org.example.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.example.result.PageResult;
import org.example.context.BaseContext;
import org.example.entity.question.Question;
import org.example.entity.response.Response;
import org.example.entity.response.ResponseItem;
import org.example.entity.survey.Survey;
import org.example.exception.QuestionNotFoundException;
import org.example.service.QuestionService;
import org.example.service.ReportService;
import org.example.service.ResponseService;
import org.example.service.SurveyService;
import org.example.vo.QuestionAnalyseVO;
import org.example.vo.SurveyAnalyseVO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Resource
    private SurveyService surveyService;

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

    @Override
    public SurveyAnalyseVO analyseSurvey(Long sid) {
        Survey survey = surveyService.getSurvey(sid);
        if (survey == null || !Objects.equals(survey.getUid(), BaseContext.getCurrentId())) {
            throw new QuestionNotFoundException("SURVEY_NOT_EXIST");
        }

        List<Response> responses = responseService.getResponseRecordsBySid(sid, true);
        AtomicLong validResponseCount = new AtomicLong();
        HashMap<Integer, Long> gradeCount = new HashMap<>();
        AtomicInteger highestGrade = new AtomicInteger(Integer.MIN_VALUE);
        AtomicInteger lowestGrade = new AtomicInteger(Integer.MAX_VALUE);
        responses.forEach(response -> {
            if (response.getGrade() == null) {
                return;
            }
            validResponseCount.getAndIncrement();
            highestGrade.set(Math.max(highestGrade.get(), response.getGrade()));
            lowestGrade.set(Math.min(lowestGrade.get(), response.getGrade()));
            gradeCount.put(response.getGrade(), gradeCount.getOrDefault(response.getGrade(), 0L) + 1);
        });

        long sum = gradeCount.entrySet().stream()
                .mapToLong(entry -> entry.getKey() * entry.getValue())
                .sum();
        double average = (double) sum / validResponseCount.get();
        return new SurveyAnalyseVO(validResponseCount.get(), gradeCount, average, highestGrade.get(), lowestGrade.get());
    }
}
