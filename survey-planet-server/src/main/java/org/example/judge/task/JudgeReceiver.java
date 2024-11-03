package org.example.judge.task;

import jakarta.annotation.Resource;
import org.example.dto.judge.JudgeReq;
import org.example.enumeration.JudgeTaskType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author chenxuanrao06@gmail.com
 * @Description: 接收评测任务统一调度
 */
@Component
public class JudgeReceiver {

    @Resource
    private JudgeDispatcher judgeDispatcher;

    // 评测任务队列，根据评测任务类型分类
    private final ConcurrentHashMap<JudgeTaskType, ConcurrentLinkedQueue<JudgeReq>> taskQueues = new ConcurrentHashMap<>();

    // 存储每个任务的 CompletableFuture
    private final ConcurrentHashMap<String, CompletableFuture<Integer>> futureMap = new ConcurrentHashMap<>();

    public CompletableFuture<Integer> addTask(JudgeReq task) {
        CompletableFuture<Integer> resultFuture = new CompletableFuture<>();
        String taskKey = String.valueOf(task.getJudge().getSubmitId()); // 或者其他唯一标识符

        // 将任务和 CompletableFuture 存储起来
        futureMap.put(taskKey, resultFuture);

        taskQueues.computeIfAbsent(task.getType(), k -> new ConcurrentLinkedQueue<>()).offer(task);
        processWaitingTask();

        return resultFuture; // 返回 CompletableFuture
    }

    @Async("judgeTaskAsyncPool")
    public void processWaitingTask() {
        handleWaitingTask(JudgeTaskType.USER_SUBMIT, JudgeTaskType.REJUDGE, JudgeTaskType.USER_TEST);
    }

    private void handleWaitingTask(JudgeTaskType... queues) {
        for (JudgeTaskType queue : queues) {
            JudgeReq task = getTask(queue);
            if (task != null) {
                handleJudge(task);
            }
        }
    }

    private JudgeReq getTask(JudgeTaskType type) {
        var taskQueue = taskQueues.get(type);
        return (taskQueue != null && !taskQueue.isEmpty()) ? taskQueue.poll() : null;
    }

    private void handleJudge(JudgeReq task) {
        // 调用 judgeDispatcher 执行评测，并获取 CompletableFuture
        CompletableFuture<Integer> resultFuture = judgeDispatcher.judge(task);

        // 根据 task 的唯一标识符找到对应的 CompletableFuture
        String taskKey = String.valueOf(task.getJudge().getSubmitId()); // 或者其他唯一标识符
        CompletableFuture<Integer> future = futureMap.remove(taskKey);

        // 将评测结果设置到对应的 CompletableFuture
        resultFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                // 处理异常情况
                if (future != null) {
                    future.completeExceptionally(throwable);
                }
            } else {
                // 设置结果
                if (future != null) {
                    future.complete(result);
                }
            }
        });

        // 继续处理下一个评测任务
        processWaitingTask();
    }
}
