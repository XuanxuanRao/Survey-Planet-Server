package org.example.service;


import org.example.dto.judge.JudgeReq;
import org.example.entity.judge.Judge;

import java.util.concurrent.CompletableFuture;

public interface JudgeService {

    Integer judge(JudgeReq judgeReq);

    void insertJudge(Judge judge);

    Judge getJudge(Long submitId);

}
