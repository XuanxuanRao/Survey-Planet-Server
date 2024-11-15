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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

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
    public void calcScore(Response response) {
        List<Question> questions = questionService.getBySid(response.getSid());

        List<CompletableFuture<Pair<Long, Integer>>> futures = questions.stream()
                .map(question -> {
                    try {
                        return doItem(response.getUid(), question,
                                response.getItems().stream()
                                        .filter(item -> item.getQid().equals(question.getQid()))
                                        .findFirst()
                                        .orElseThrow(),
                                false);
                    } catch (Exception e) {
                        log.error("Error when scoring response", e);
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        // 使用 CompletableFuture.allOf 等待所有任务完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.thenRun(() -> {
            try {
                int grade = futures.stream()
                        .map(CompletableFuture::join) // 等待所有异步任务完成并获取结果
                        .filter(Objects::nonNull) // 过滤掉 null 值
                        .peek(pair -> responseMapper.setItemGrade(pair.getKey(), pair.getValue()))
                        .mapToInt(pair -> Optional.ofNullable(pair.getValue()).orElse(0))
                        .sum();
                log.info("Grade of response {} is {}", response.getRid(), grade);
                responseMapper.setRecordGrade(response.getRid(), grade);
            } catch (Exception e) {
                log.error("Error when scoring response", e);
            }
        });
    }

    @Override
    public void reCalcScore(Response response, List<ResponseItem> changedItems) {
        Integer originGrade = response.getGrade();

        List<Question> questions = changedItems.stream().map(item -> questionService.getByQid(item.getQid())).toList();

        int sum1 = response.getItems().stream()
                .filter(item -> changedItems.contains(item) && item.getGrade() != null)
                .mapToInt(ResponseItem::getGrade)
                .sum();

        List<CompletableFuture<Pair<Long, Integer>>> futures = questions.stream()
                .map(question -> {
                    try {
                        return doItem(response.getUid(), question,
                                changedItems.stream()
                                        .filter(item -> item.getQid().equals(question.getQid()))
                                        .findFirst()
                                        .orElseThrow(),
                                true);
                    } catch (Exception e) {
                        log.error("Error when scoring response", e);
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.thenRun(() -> {
            try {
                int sum2 = futures.stream()
                        .map(CompletableFuture::join) // 等待所有异步任务完成并获取结果
                        .filter(Objects::nonNull) // 过滤掉 null 值
                        .peek(pair -> responseMapper.setItemGrade(pair.getKey(), pair.getValue()))
                        .mapToInt(pair -> Optional.ofNullable(pair.getValue()).orElse(0))
                        .sum();
                log.info("Grade of response {} is {}", response.getRid(), originGrade + sum2 - sum1);
                responseMapper.setRecordGrade(response.getRid(), originGrade + sum2 - sum1);
            } catch (Exception e) {
                log.error("Error when scoring response", e);
            }
        });
    }

    private CompletableFuture<Pair<Long, Integer>> doItem(Long uid, Question question, ResponseItem item, boolean isReCalc) {
        // 公共方法，用于包装结果
        final BiFunction<Long, Integer, CompletableFuture<Pair<Long, Integer>>> result =
                (submitId, score) -> CompletableFuture.completedFuture(Pair.of(submitId, score));

        // 根据题型判断
        if (question.getScore() == null) {
            return result.apply(item.getSubmitId(), null);
        } else if (question.getScore() == 0) {
            return result.apply(item.getSubmitId(), 0);
        }

        if (question instanceof SingleChoiceQuestion scq) {
            int score = item.getContent().get(0).equals(scq.getAnswer().get(0)) ? scq.getScore() : 0;
            return result.apply(item.getSubmitId(), score);
        } else if (question instanceof MultipleChoiceQuestion mcq) {
            boolean isCorrect = item.getContent().size() == mcq.getAnswer().size()
                    && new HashSet<>(item.getContent()).containsAll(mcq.getAnswer());
            int score = isCorrect ? mcq.getScore() : 0;
            return result.apply(item.getSubmitId(), score);
        } else if (question instanceof FillBlankQuestion fbq) {
            int score = item.getContent().get(0).equals(fbq.getAnswer().get(0)) ? fbq.getScore() : 0;
            return result.apply(item.getSubmitId(), score);
        } else if (question instanceof FileQuestion fq) {
            return result.apply(item.getSubmitId(), null);
        } else if (question instanceof CodeQuestion cq) {
            Judge judge = Judge.builder()
                    .submitId(item.getSubmitId())
                    .qid(question.getQid())
                    .uid(uid)
                    .language(item.getContent().get(1))
                    .codeContent(aliOSSUtil.download(item.getContent().get(0)))
                    .build();

            return CompletableFuture.supplyAsync(() -> {
                CompletableFuture<Integer> scoreFuture = judgeReceiver.addTask(new JudgeReq(isReCalc ? JudgeTaskType.REJUDGE : JudgeTaskType.USER_SUBMIT, judge));
                return scoreFuture.join(); // 阻塞获取评测结果
            }).exceptionally(ex -> {
                log.error("Error during judgment task: {}", ex.getMessage(), ex);
                return 0;
            }).thenApply(score -> Pair.of(item.getSubmitId(), score));
        }
        throw new IllegalArgumentException("Unknown question type: " + question.getClass().getName());
    }


}
