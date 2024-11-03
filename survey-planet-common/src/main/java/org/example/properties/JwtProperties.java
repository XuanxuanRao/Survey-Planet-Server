package org.example.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author chenxuanrao06@gmail.com
 * @Description: jwt属性配置
 */

@Component
@Data
@ConfigurationProperties(prefix = "survey-planet.jwt")
public class JwtProperties {
    private String secretKey;
    private long ttl;
    private String tokenName;
}
