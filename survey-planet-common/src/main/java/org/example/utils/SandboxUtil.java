package org.example.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.example.enumeration.JudgeStatus;
import org.example.exception.SystemError;
import org.example.properties.GoJudgeProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description: go-judge 运行工具类，用于向 go-judge 服务发送请求。
 * <p> go-judge 是一个沙盒服务，用于编译、运行代码。
 */
@Slf4j
@Component
public class SandboxUtil {
    private final GoJudgeProperties goJudgeProperties;

    private final RestTemplate restTemplate;

    private final JSONArray COMPILE_FILES = new JSONArray();

    @Autowired
    public SandboxUtil(GoJudgeProperties goJudgeProperties) {
        this.goJudgeProperties = goJudgeProperties;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(20000);
        requestFactory.setReadTimeout(180000);
        this.restTemplate = new RestTemplate(requestFactory);

        JSONObject content = new JSONObject();
        content.set("content", "");

        JSONObject stdout = new JSONObject();
        stdout.set("name", "stdout");
        stdout.set("max", 1024 * 1024 * goJudgeProperties.getStdioSizeMb());

        JSONObject stderr = new JSONObject();
        stderr.set("name", "stderr");
        stderr.set("max", 1024 * 1024 * goJudgeProperties.getStdioSizeMb());
        COMPILE_FILES.put(content);
        COMPILE_FILES.put(stdout);
        COMPILE_FILES.put(stderr);
    }

    public static final HashMap<String, Integer> RESULT_MAP_STATUS = new HashMap<>() {{
        put("Time Limit Exceeded", JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        put("Memory Limit Exceeded", JudgeStatus.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus());
        put("Output Limit Exceeded", JudgeStatus.STATUS_RUNTIME_ERROR.getStatus());
        put("Accepted", JudgeStatus.STATUS_ACCEPTED.getStatus());
        put("Nonzero Exit Status", JudgeStatus.STATUS_RUNTIME_ERROR.getStatus());
        put("Internal Error", JudgeStatus.STATUS_SYSTEM_ERROR.getStatus());
        put("File Error", JudgeStatus.STATUS_SYSTEM_ERROR.getStatus());
        put("Signalled", JudgeStatus.STATUS_RUNTIME_ERROR.getStatus());
    }};

    public static final List<String> signals = Arrays.asList(
            "", // 0
            "Hangup", // 1
            "Interrupt", // 2
            "Quit", // 3
            "Illegal instruction", // 4
            "Trace/breakpoint trap", // 5
            "Aborted", // 6
            "Bus error", // 7
            "Floating point exception", // 8
            "Killed", // 9
            "User defined signal 1", // 10
            "Segmentation fault", // 11
            "User defined signal 2", // 12
            "Broken pipe", // 13
            "Alarm clock", // 14
            "Terminated", // 15
            "Stack fault", // 16
            "Child exited", // 17
            "Continued", // 18
            "Stopped (signal)", // 19
            "Stopped", // 20
            "Stopped (tty input)", // 21
            "Stopped (tty output)", // 22
            "Urgent I/O condition", // 23
            "CPU time limit exceeded", // 24
            "File size limit exceeded", // 25
            "Virtual timer expired", // 26
            "Profiling timer expired", // 27
            "Window changed", // 28
            "I/O possible", // 29
            "Power failure", // 30
            "Bad system call" // 31
    );

    private JSONArray run(String uri, JSONObject param) throws SystemError {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(JSONUtil.toJsonStr(param), headers);
        ResponseEntity<String> postForEntity;
        try {
            postForEntity = restTemplate.postForEntity(goJudgeProperties.getBaseUrl() + uri, request, String.class);
            return JSONUtil.parseArray(postForEntity.getBody());
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() != 200) {
                throw new SystemError("Cannot connect to sandbox service.", null, ex.getResponseBodyAsString());
            }
        } catch (Exception e) {
            throw new SystemError("Call SandBox Error.", null, e.getMessage());
        }
        return null;
    }

    public void delFile(String fileId) {
        try {
            restTemplate.delete(goJudgeProperties.getBaseUrl() + "/file/{0}", fileId);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() != 200) {
                log.error("Sandbox {}", ex.getResponseBodyAsString());
            }
        }
    }


    /**
     * Key method the judge system.
     * <p> 编译代码得到可执行程序
     * @param maxCpuTime        最大编译的cpu时间 ms
     * @param maxRealTime       最大编译的真实时间 ms
     * @param maxMemory         最大编译的空间 b
     * @param maxStack          最大编译的栈空间 b
     * @param srcName           编译的源文件名字
     * @param exeName           编译生成的exe文件名字
     * @param args              编译的cmd参数
     * @param envs              编译的环境变量
     * @param code              编译的源代码
     * @param needCopyOutCached 是否需要生成用户程序的缓存文件，即生成用户程序id
     * @param needCopyOutExe    是否需要生成编译后的用户程序exe文件
     * @param copyOutDir        生成编译后的用户程序exe文件的指定路径
     * @return  返回Sandbox编译结果（json格式）
     * @throws SystemError  系统错误
     */
    public JSONArray compile(Long maxCpuTime,
                                    Long maxRealTime,
                                    Long maxMemory,
                                    Long maxStack,
                                    String srcName,
                                    String exeName,
                                    List<String> args,
                                    List<String> envs,
                                    String code,
                                    Boolean needCopyOutCached,
                                    Boolean needCopyOutExe,
                                    String copyOutDir) throws SystemError {
        JSONObject cmd = new JSONObject();
        cmd.set("args", args);
        cmd.set("env", envs);
        cmd.set("files", COMPILE_FILES);
        // ms-->ns
        cmd.set("cpuLimit", maxCpuTime * 1000 * 1000L);
        cmd.set("clockLimit", maxRealTime * 1000 * 1000L);
        // byte
        cmd.set("memoryLimit", maxMemory);
        cmd.set("procLimit", goJudgeProperties.getMaxProcessNumber());
        cmd.set("stackLimit", maxStack);

        JSONObject fileContent = new JSONObject();
        fileContent.set("content", code);

        JSONObject copyIn = new JSONObject();
        copyIn.set(srcName, fileContent);

        cmd.set("copyIn", copyIn);
        cmd.set("copyOut", new JSONArray().put("stdout").put("stderr"));

        if (needCopyOutCached) {
            cmd.set("copyOutCached", new JSONArray().put(exeName));
        }

        if (needCopyOutExe) {
            cmd.set("copyOutDir", copyOutDir);
        }

        return sendRunRequest(cmd);
    }


    /**
     * 运行一个测试用例，得到 go-judge 的评测结果。
     * <p> 输入数据由参数 testCasePath 或 testCaseContent 其中之一指定，两者必然一个为空，一个不为空。
     * SurveyPlanet 使用 OSS 进行文件服务，所以输入数据是从 OSS 获取的文件内容，即 testCaseContent
     * @param args            评测运行cmd的命令参数
     * @param envs            评测运行的环境变量
     * @param testCasePath    题目数据的输入文件路径
     * @param testCaseContent 题目数据的输入数据（与testCasePath二者选一）
     * @param maxTime         评测的最大限制时间 ms
     * @param maxOutputSize   评测的最大输出大小 kb
     * @param maxStack        评测的最大限制栈空间 mb
     * @param exeName         评测的用户程序名称
     * @param fileId          评测的用户程序文件id
     * @param fileContent     评测的用户程序文件内容，如果userFileId存在则为null
     * @param isFileIO        是否为文件IO
     * @param ioReadFileName  题目指定的io输入文件的名称
     * @param ioWriteFileName 题目指定的io输出文件的名称
     * @return 返回Sandbox评测结果（json格式）
     * @throws SystemError  系统错误
     */
    public JSONArray testCase(List<String> args,
                                     List<String> envs,
                                     String testCasePath,
                                     String testCaseContent,
                                     Long maxTime,
                                     Long maxMemory,
                                     Long maxOutputSize,
                                     Integer maxStack,
                                     String exeName,
                                     String fileId,
                                     String fileContent,
                                     Boolean isFileIO,
                                     String ioReadFileName,
                                     String ioWriteFileName) throws SystemError {

        JSONObject cmd = new JSONObject();
        cmd.set("args", args);
        cmd.set("env", envs);

        JSONArray files = new JSONArray();

        // 设置输入数据
        JSONObject testCaseInput = new JSONObject();
        // todo: prod 环境下要修改
        if (StringUtils.hasLength(testCaseContent)) {
            testCaseInput.set("content", testCaseContent);
        } else {
            testCaseInput.set("src", testCasePath);
        }

        if (BooleanUtils.isFalse(isFileIO)) {
            files.put(testCaseInput);
            JSONObject stdout = new JSONObject();
            stdout.set("name", "stdout");
            stdout.set("max", maxOutputSize);
            files.put(stdout);
        }

        JSONObject stderr = new JSONObject();
        stderr.set("name", "stderr");
        stderr.set("max", 1024 * 1024 * 16);
        files.put(stderr);

        cmd.set("files", files);

        // ms-->ns
        cmd.set("cpuLimit", maxTime * 1000 * 1000L);
        cmd.set("clockLimit", maxTime * 1000 * 1000L * 3);
        // byte
        cmd.set("memoryLimit", (maxMemory + 100) * 1024 * 1024L);
        cmd.set("procLimit", goJudgeProperties.getMaxProcessNumber());
        cmd.set("stackLimit", maxStack * 1024 * 1024L);

        // 设置要运行的程序
        JSONObject exeFile = new JSONObject();
        if (StringUtils.hasLength(fileId)) {
            exeFile.set("fileId", fileId);
        } else {
            exeFile.set("content", fileContent);
        }
        JSONObject copyIn = new JSONObject();
        copyIn.set(exeName, exeFile);

        JSONArray copyOut = new JSONArray();
        copyOut.put("stderr");
        if (BooleanUtils.isFalse(isFileIO)){
            copyOut.put("stdout");
        } else{
            copyIn.set(ioReadFileName, testCaseInput);
            // 在文件名之后加入 '?' 来使文件变为可选，可选文件不存在的情况不会触发 FileError
            copyOut.put(ioWriteFileName + "?");
        }

        cmd.set("copyIn", copyIn);
        cmd.set("copyOut", copyOut);

        return sendRunRequest(cmd);
    }


    /**
     * A helper method for calling the method {@link #run(String, JSONObject)}.
     * @param cmd the argument in the request body
     * @return the response from go-judge
     * @throws SystemError if the request failed
     */
    private JSONArray sendRunRequest(JSONObject cmd) throws SystemError {
        JSONObject param = new JSONObject();
        param.set("cmd", new JSONArray().put(cmd));

        JSONArray result = run("/run", param);
        assert result != null;
        JSONObject compileRes = (JSONObject) result.get(0);
        compileRes.set("originalStatus", compileRes.getStr("status"));
        compileRes.set("status", RESULT_MAP_STATUS.get(compileRes.getStr("status")));
        return result;
    }


}
