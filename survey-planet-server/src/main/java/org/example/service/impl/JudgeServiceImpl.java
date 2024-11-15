package org.example.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.judge.JudgeReq;
import org.example.entity.judge.Judge;
import org.example.entity.question.CodeQuestion;
import org.example.judge.JudgeContext;
import org.example.mapper.JudgeMapper;
import org.example.service.JudgeService;
import org.example.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Slf4j
@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private JudgeContext judgeContext;

    @Resource
    private QuestionService questionService;

    @Resource
    private JudgeMapper judgeMapper;

    @Override
    @Transactional
    public Integer judge(JudgeReq judgeReq) {
        Judge judge = judgeReq.getJudge();
        CodeQuestion problem = (CodeQuestion) questionService.getByQid(judge.getQid());
        Judge result = judgeContext.judge(problem, judge);

        insertJudge(result);
        return result.getScore();
    }

    @Override
    public void insertJudge(Judge judge) {
        if (getJudge(judge.getSubmitId()) != null) {
            judgeMapper.updateJudgeResult(judge);
        } else {
            judgeMapper.addJudgeResult(judge);
        }
        if (judge.getCaseJudgeResults() != null && !judge.getCaseJudgeResults().isEmpty()) {
            judgeMapper.addCaseResult(judge.getCaseJudgeResults());
        }
    }

    @Override
    public Judge getJudge(Long submitId) {
        return judgeMapper.getJudgeBySubmitId(submitId);
    }
}
