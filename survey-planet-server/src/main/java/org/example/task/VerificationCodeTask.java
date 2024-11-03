package org.example.task;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.service.VerificationCodeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VerificationCodeTask {
    @Resource
    private VerificationCodeService verificationCodeService;

    /**
     * Clear verification expired code every 15 minutes
     */
    @Scheduled(cron = "0 0/15 * * * ?")
    public void clearVerificationCode() {
        Integer number = verificationCodeService.clear();
        if (number > 0) {
            log.info("clear {} expired verification code", number);
        }
    }
}
