package org.example;

import jakarta.annotation.Resource;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.mapper.ResponseMapper;
import org.example.utils.AliOSSUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@SpringBootTest
class SurveyPlanetServerApplicationTests {

    @Resource
    AliOSSUtil aliOSSUtil;

    @Resource
    ResponseMapper responseMapper;

    @Test
    void download() {
        String content = aliOSSUtil.download("https://survey-planet-test.oss-cn-beijing.aliyuncs.com/24c85e4e-d4b8-4cab-874e-704c5a7bcad0.sql");
        System.out.println(content);
    }

    @Test
    void translate() {
        // System.out.println(translateCommandline("/bin/bash -c \"javac -encoding utf-8 {src_path} && jar -cvf {exe_path} *.class\""));
        System.out.println(parseCommand("/bin/bash -c \"javac -encoding utf-8 {src_path} && jar -cvf {exe_path} *.class\""));
    }


    private static List<String> parseCommand(String command) {
        if (command == null || command.isEmpty()) {
            return new ArrayList<>();
        }

        int state = 0;
        StringTokenizer tok = new StringTokenizer(command, "\"' ", true);
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
                case 1:
                    if ("'".equals(nextTok)) {
                        state = 0;
                    } else {
                        current.append(nextTok);
                    }
                    continue;
                case 2:
                    if ("\"".equals(nextTok)) {
                        state = 0;
                    } else {
                        current.append(nextTok);
                    }
                    continue;
            }

            if ("'".equals(nextTok)) {
                state = 1;
            } else if ("\"".equals(nextTok)) {
                state = 2;
            } else if (" ".equals(nextTok)) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(nextTok);
            }

        }

        result.add(current.toString());

        if (state != 1 && state != 2) {
            return result;
        }
        throw new RuntimeException("unbalanced quotes in " + command);
    }

}
