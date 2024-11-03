package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync(proxyTargetClass=true) // 开启异步
@EnableScheduling
@EnableTransactionManagement
public class SurveyPlanetApplication {

    public static void main(String[] args) {
        SpringApplication.run(SurveyPlanetApplication.class, args);
    }

}
