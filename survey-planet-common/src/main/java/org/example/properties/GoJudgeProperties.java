package org.example.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author chenxuanrao06@gmail.com
 * @Description: go-judge 属性配置
 */

@Component
@Data
@ConfigurationProperties(prefix = "survey-planet.go-judge")
public class GoJudgeProperties {
    private String baseUrl;
    private int maxProcessNumber;
    private int timeLimitMs;
    private int memoryLimitMb;
    private int stackLimitMb;
    private int stdioSizeMb;
}
