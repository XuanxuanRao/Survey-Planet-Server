package org.example.judge.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.example.constant.JudgeConstant;
import org.example.entity.question.CodeQuestion;
import org.example.exception.SystemError;
import org.example.service.QuestionService;
import org.example.utils.AliOSSUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Component
public class TestCaseUtil {

    @Resource
    private QuestionService questionService;

    @Resource
    private AliOSSUtil aliOSSUtil;

    private final static Pattern EOL_PATTERN = Pattern.compile("[^\\S\\n]+(?=\\n)");

    public JSONObject loadTestCasesInfo(final Long qid, String testCasesDir) throws SystemError {
        if (FileUtil.exist(testCasesDir + File.separator + "info")) {
            FileReader fileReader = new FileReader(testCasesDir + File.separator + "info", CharsetUtil.UTF_8);
            // todo: 需要允许测试数据更新
            String infoStr = fileReader.readString();
            return JSONUtil.parseObj(infoStr);
        } else {
            List<HashMap<String, Object>> testCases = new LinkedList<>();
            // 从 OSS 加载测试数据
            CodeQuestion problem = (CodeQuestion) questionService.getByQid(qid);
            List<String> inputFileOSSUrl = problem.getInputFileUrls();
            List<String> outputFileOSSUrl = problem.getOutputFileUrls();
            for (int i = 0; i < inputFileOSSUrl.size(); i++) {
                HashMap<String, Object> testCase = new HashMap<>();
                testCase.put("caseId", i + 1);
                testCase.put("input", aliOSSUtil.download(inputFileOSSUrl.get(i)));
                testCase.put("output", aliOSSUtil.download(outputFileOSSUrl.get(i)));
                testCases.add(testCase);
            }
            return buildTestCasesJSONO(qid, testCases);
        }
    }

    private JSONObject buildTestCasesJSONO(final Long qid, final List<HashMap<String, Object>> testCases) throws SystemError {
        if (testCases == null || testCases.isEmpty()) {
            throw new SystemError("Problem " + qid + " doesn't have test case", null, "Test case(s) does not exist");
        }

        JSONObject result = new JSONObject();

        result.set("testCasesSize", testCases.size());
        JSONArray testCaseList = new JSONArray(testCases.size());
        String testCasesDir = JudgeConstant.TEST_CASE_DIR + "/problem_" + qid;
        FileUtil.del(testCasesDir);
        for (int i = 0; i < testCases.size(); i++) {
            testCaseList.add(buildTestCaseJSON(testCases, i, testCasesDir));
        }
        result.set("testCases", testCaseList);
        return result;
    }

    private JSONObject buildTestCaseJSON(List<HashMap<String, Object>> testCases, final int index, String testCasesDir) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("caseId", testCases.get(index).get("caseId"));

        // 生成输入文件
        String inputName = (index + 1) + ".in";
        jsonObject.set("inputName", inputName);
        FileWriter infileWriter = new FileWriter(testCasesDir + "/" + inputName, CharsetUtil.UTF_8);
        // 将该测试数据的输入写入到文件
        infileWriter.write((String) testCases.get(index).get("input"));

        // 生成输出文件
        String outputName = (index + 1) + ".out";
        jsonObject.set("outputName", outputName);
        String outputData = (String) testCases.get(index).get("output");
        FileWriter outFile = new FileWriter(testCasesDir + "/" + outputName, CharsetUtil.UTF_8);
        outFile.write(outputData);

        // 原数据MD5
        jsonObject.set("outputMd5", DigestUtils.md5DigestAsHex(outputData.getBytes(StandardCharsets.UTF_8)));
        // 原数据大小
        jsonObject.set("outputSize", outputData.getBytes(StandardCharsets.UTF_8).length);
        // 去掉全部空格的MD5，用来判断 pe
        jsonObject.set("allStrippedOutputMd5", DigestUtils.md5DigestAsHex(outputData.replaceAll("\\s+", "").getBytes(StandardCharsets.UTF_8)));
        // 默认去掉文末空格的MD5
        jsonObject.set("EOFStrippedOutputMd5", DigestUtils.md5DigestAsHex(rtrim(outputData).getBytes(StandardCharsets.UTF_8)));

        return jsonObject;
    }


    public String rtrim(String value) {
        if (value == null) return null;
        return EOL_PATTERN.matcher(StrUtil.trimEnd(value)).replaceAll("");
    }

}
