package org.example.service.impl;

import cn.hutool.core.lang.Pair;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.judge.JudgeReq;
import org.example.entity.judge.Judge;
import org.example.entity.question.*;
import org.example.entity.response.Response;
import org.example.entity.response.ResponseItem;
import org.example.enumeration.JudgeTaskType;
import org.example.judge.task.JudgeReceiver;
import org.example.mapper.ResponseMapper;
import org.example.service.QuestionService;
import org.example.service.ScoringService;
import org.example.utils.AliOSSUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Service
@Slf4j
public class ScoringServiceImpl implements ScoringService {
    @Resource
    private QuestionService questionService;

    @Resource
    private JudgeReceiver judgeReceiver;

    @Resource
    private AliOSSUtil aliOSSUtil;

    @Resource
    private ResponseMapper responseMapper;

    @Override
    @Async
    public void getScore(Response response) {
        List<Question> questions = questionService.getBySid(response.getSid());

        List<CompletableFuture<Pair<Long, Integer>>> futures = questions.stream()
                .map(question -> doItem(response.getUid(), question, response.getItems().stream().filter(item -> item.getQid().equals(question.getQid())).findFirst().orElseThrow()))
                .toList();

        // 使用 CompletableFuture.allOf 等待所有任务完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        allOf.thenRun(() -> {
            int grade = futures.stream()
                    .map(CompletableFuture::join) // 等待所有异步任务完成并获取结果
                    .filter(Objects::nonNull) // 过滤掉 null 值
                    .peek(pair -> responseMapper.setItemGrade(pair.getKey(), pair.getValue()))
                    .mapToInt(Pair::getValue)
                    .sum();
            log.info("Grade of response {} is {}", response.getRid(), grade);
            responseMapper.setRecordGrade(response.getRid(), grade);
        });
    }



    private CompletableFuture<Pair<Long, Integer>> doItem(Long uid, Question question, ResponseItem item) {
        if (question.getScore() == null) {
            return CompletableFuture.completedFuture(Pair.of(item.getSubmitId(), null));
        } else if (question.getScore() == 0) {
            return CompletableFuture.completedFuture(Pair.of(item.getSubmitId(), 0));
        }

        if (question instanceof SingleChoiceQuestion scq) {
            return CompletableFuture.completedFuture(Pair.of(
                    item.getSubmitId(),
                    item.getContent().get(0).equals(scq.getAnswer().get(0)) ? scq.getScore() : 0
            ));
        } else if (question instanceof MultipleChoiceQuestion mcq) {
            return CompletableFuture.completedFuture(Pair.of(
                    item.getSubmitId(),
                    item.getContent().size() == mcq.getAnswer().size() && new HashSet<>(item.getContent()).containsAll(mcq.getAnswer()) ? mcq.getScore() : 0
            ));
        } else if (question instanceof FillBlankQuestion fbq) {
            return CompletableFuture.completedFuture(Pair.of(
                    item.getSubmitId(),
                    item.getContent().get(0).equals(fbq.getAnswer().get(0)) ? fbq.getScore() : 0
            ));
        } else if (question instanceof FileQuestion fq) {
            return CompletableFuture.completedFuture(Pair.of(item.getSubmitId(), null));
        } else if (question instanceof CodeQuestion cq) {
            Judge judge = Judge.builder()
                    .submitId(item.getSubmitId())
                    .qid(question.getQid())
                    .uid(uid)
                    .language(item.getContent().get(1))
                    .codeContent(aliOSSUtil.download(item.getContent().get(0)))
                    .build();

            return CompletableFuture.supplyAsync(() -> {
                // 添加评测任务并获取其返回的 CompletableFuture
                CompletableFuture<Integer> scoreFuture = judgeReceiver.addTask(new JudgeReq(JudgeTaskType.USER_SUBMIT, judge));

                // 这里可以返回 scoreFuture 的结果
                return scoreFuture.join(); // 阻塞等待评测结果
            }).thenApply(score -> Pair.of(item.getSubmitId(), score));
        }
        throw new IllegalArgumentException("Unknown question type: " + question.getClass().getName());
    }

}
