package org.example.controller;

import jakarta.annotation.Resource;
import org.example.result.PageResult;
import org.example.result.Result;
import org.example.entity.response.ResponseItem;
import org.example.service.ReportService;
import org.example.vo.QuestionAnalyseVO;
import org.example.vo.SurveyAnalyseVO;
import org.springframework.web.bind.annotation.*;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Resource
    private ReportService reportService;

    @GetMapping("/stat/question/{qid}")
    public Result<QuestionAnalyseVO> analyseQuestion(@PathVariable Long qid) {
        return Result.success(reportService.analyseQuestion(qid));
    }

    @GetMapping("/detail/{qid}")
    public Result<PageResult<ResponseItem>> getQuestionResponses(@PathVariable Long qid, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        return Result.success(reportService.getQuestionDetail(qid, pageNum, pageSize));
    }

    @GetMapping("/stat/survey/{sid}")
    public Result<SurveyAnalyseVO> analyseSurvey(@PathVariable Long sid) {
        return Result.success(reportService.analyseSurvey(sid));
    }

}
