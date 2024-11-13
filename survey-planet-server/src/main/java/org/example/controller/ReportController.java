package org.example.controller;

import jakarta.annotation.Resource;
import org.example.Result.Result;
import org.example.service.ReportService;
import org.example.service.ResponseService;
import org.example.vo.QuestionAnalyseVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Resource
    private ResponseService responseService;

    @Resource
    private ReportService reportService;

    @GetMapping("/{qid}")
    public Result<QuestionAnalyseVO> analyseQuestion(@PathVariable Long qid) {
        return Result.success(reportService.analyseQuestion(qid));
    }

}
