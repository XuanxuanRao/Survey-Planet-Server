package org.example.utils;

import java.util.concurrent.*;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
public class ThreadPoolUtil {

    private static ExecutorService executorService;
    private static final int cpuNum = Runtime.getRuntime().availableProcessors();

    private ThreadPoolUtil() {
        //手动创建线程池.
        executorService = new ThreadPoolExecutor(
                cpuNum, // 核心线程数
                cpuNum, // 最大线程数
                3,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(200 * cpuNum),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    private static class PluginConfigHolder {
        private final static ThreadPoolUtil INSTANCE = new ThreadPoolUtil();
    }

    public static ThreadPoolUtil getInstance() {
        return PluginConfigHolder.INSTANCE;
    }

    public ExecutorService getThreadPool() {
        return executorService;
    }

}
