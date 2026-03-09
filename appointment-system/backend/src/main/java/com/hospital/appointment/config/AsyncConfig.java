package com.hospital.appointment.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 企业级异步线程池配置
 * 包含：MDC 线程上下文的跨线程传递（解决子线程日志丢失 TraceId 的问题）
 */
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean("auditThreadPool")
    public Executor auditThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(10);
        // 队列容量
        executor.setQueueCapacity(200);
        // 线程名称前缀
        executor.setThreadNamePrefix("audit-task-");
        // 拒绝策略：由调用线程（提交任务的线程）直接执行该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 【核心神技】：设置任务装饰器，将主线程的 MDC 复制到子线程中
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    /**
     * MDC 上下文拷贝装饰器
     */
    static class MdcTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            // 获取主线程的 MDC 上下文
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    // 在子线程开始执行前，将主线程的上下文还原给子线程
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    // 子线程执行完毕，同样必须清理
                    MDC.clear();
                }
            };
        }
    }
}