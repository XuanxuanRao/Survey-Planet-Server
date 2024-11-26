package org.example;

import jakarta.annotation.Resource;

import org.example.utils.AliOSSUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class SurveyPlanetServerApplicationTests {

    @Resource
    AliOSSUtil aliOSSUtil;

    @Test
    void download() {
        String content = aliOSSUtil.download("https://survey-planet-test.oss-cn-beijing.aliyuncs.com/24c85e4e-d4b8-4cab-874e-704c5a7bcad0.sql");
        System.out.println(content);
    }
}
