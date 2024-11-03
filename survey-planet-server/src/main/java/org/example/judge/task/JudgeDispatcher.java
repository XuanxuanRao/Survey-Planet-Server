package org.example.judge.task;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import org.example.dto.judge.JudgeReq;
import org.example.service.JudgeService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Component
public class JudgeDispatcher {
    @Resource
    private JudgeService judgeService;

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);
    private final static Map<String, Future> futureTaskMap = new ConcurrentHashMap<>(20);


    public CompletableFuture<Integer> judge(JudgeReq judgeReq) {
        String taskKey = UUID.randomUUID().toString() + judgeReq.getJudge().getSubmitId();
        CompletableFuture<Integer> resultFuture = new CompletableFuture<>();

        Runnable getResultTask = () -> {
            try {
                Integer result = judgeService.judge(judgeReq); // 获取结果
                resultFuture.complete(result); // 设置结果到 CompletableFuture
            } catch (Exception e) {
                resultFuture.completeExceptionally(e); // 处理异常
            } finally {
                releaseTaskThread(taskKey); // 释放任务线程
            }
        };

        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(getResultTask, 0, 2, TimeUnit.SECONDS);
        futureTaskMap.put(taskKey, scheduledFuture);

        return resultFuture;
    }


    /**
     * 释放评测任务线程
     * @param taskKey 任务key
     */
    private void releaseTaskThread(String taskKey) {
        Future future = futureTaskMap.get(taskKey);
        if (future != null) {
            boolean isCanceled = future.cancel(true);
            if (isCanceled) {
                futureTaskMap.remove(taskKey);
            }
        }
    }
}
