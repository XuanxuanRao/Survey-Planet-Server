package org.example.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.result.Result;
import org.example.annotation.ControllerLog;
import org.example.entity.judge.Judge;
import org.example.entity.response.Response;
import org.example.entity.response.ResponseItem;
import org.example.service.JudgeService;
import org.example.service.ResponseService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */

@Slf4j
@RestController
@RequestMapping("/api")
public class JudgeController {

    @Resource
    private JudgeService judgeService;

    @Resource
    private ResponseService responseService;

    @PostMapping("/rejudge/{submitId}")
    @ControllerLog(name = "rejudge")
    public Result<Void> rejudge(@PathVariable Long submitId) {
        Judge judge = judgeService.getJudge(submitId);
        Response response = responseService.getResponseBySubmitId(judge.getSubmitId());
        ResponseItem codeItem = response.getItems().stream()
                .filter(item -> item.getSubmitId().equals(judge.getSubmitId()))
                .findFirst()
                .orElseThrow();

        responseService.updateResponse(response.getRid(), List.of(codeItem), null);

        return Result.success();
    }
}
