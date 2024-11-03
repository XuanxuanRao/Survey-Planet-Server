package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.example.properties.AliOSSProperties;
import org.example.utils.AliOSSUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OSSConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AliOSSUtil aliOssUtil(AliOSSProperties aliOssProperties){
        return new AliOSSUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }
}
