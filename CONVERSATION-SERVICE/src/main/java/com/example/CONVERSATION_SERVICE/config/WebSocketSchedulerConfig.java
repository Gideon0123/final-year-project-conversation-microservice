package com.example.CONVERSATION_SERVICE.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class WebSocketSchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler stompTaskScheduler() {

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix(
                "stomp-heartbeat-"
        );

        scheduler.initialize();

        return scheduler;
    }
}
