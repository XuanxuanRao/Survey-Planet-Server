package org.example.task;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.service.SurveyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SurveyTask {

    @Resource
    private SurveyService surveyService;

    /**
     * clear the survey deleted by the user every day
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void clearSurvey() {
        int number = surveyService.clearSurvey();
        if (number > 0) {
            log.info("Clear {} surveys", number);
        }
    }
}
