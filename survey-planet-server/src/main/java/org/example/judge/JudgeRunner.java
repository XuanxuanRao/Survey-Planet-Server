package org.example.judge;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.JudgeConstant;
import org.example.dto.judge.GlobalJudgeDTO;
import org.example.dto.judge.CaseJudgeDTO;
import org.example.dto.judge.SandboxResult;
import org.example.entity.question.CodeQuestion;
import org.example.enumeration.JudgeStatus;
import org.example.exception.SystemError;
import org.example.judge.utils.TestCaseUtil;
import org.example.utils.AliOSSUtil;
import org.example.utils.SandboxUtil;
import org.example.utils.ThreadPoolUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author chenxuanrao06@gmail.com
 * @Description: 运行一次提交，进行评测，并返回评测结果
 */
@Component
@Slf4j
public class JudgeRunner {
    @Resource
    private LanguageConfigLoader languageConfigLoader;

    @Resource
    private TestCaseUtil testCaseUtil;

    @Resource
    private SandboxUtil sandboxUtil;

    @Resource
    private AliOSSUtil aliOSSUtil;


    /**
     * Key method of the judge system.
     * <p> 对一个提交进行评测，会运行所有测试点并以列表形式返回每个测试点的评测结果
     * @param submitId  提交ID
     * @param problem 题目信息
     * @param testCasesDir 测试用例文件夹
     * @param language  编程语言
     * @param testCasesInfo 测试用例信息
     * @param userFileId    用户文件ID（获取编译后的可执行程序）
     * @param userFileContent 用户代码内容（无法编译得到可执行程序的代码）
     * @param needUserOutput 是否获取用户输出
     * @return 评测结果
     * @throws SystemError
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<JSONObject> judgeAllCase(Long submitId,
                                         CodeQuestion problem,
                                         String testCasesDir,
                                         String language,
                                         JSONObject testCasesInfo,
                                         Boolean needUserOutput,
                                         String userFileId,
                                         String userFileContent) throws SystemError, ExecutionException, InterruptedException {
        if (testCasesInfo == null) {
            throw new SystemError("Testcase(s) for problem does not exist", null, null);
        }

        JSONArray testcaseList = (JSONArray) testCasesInfo.get("testCases");

        Long testTime = (long) problem.getTimeLimit() + JudgeConstant.COMPILE_TIME;

        // 用户输出的文件夹
        String runDir = JudgeConstant.RUN_WORKPLACE_DIR + File.separator + submitId;

        GlobalJudgeDTO judgeGlobalDTO = GlobalJudgeDTO.builder()
                .qid(problem.getQid())
                .userFileId(userFileId)
                .userFileContent(userFileContent)
                .runDir(runDir)
                .testTime(testTime)
                .maxMemory((long) problem.getMemoryLimit())
                .maxTime((long) problem.getTimeLimit())
                .maxStack(problem.getStackLimit())
                .testCaseInfo(testCasesInfo)
                .runConfig(languageConfigLoader.getLanguageConfigByName(language))
                .removeEOLBlank(problem.getIsRemoveEndBlank())
                .needUserOutput(needUserOutput)
                .build();

        // 运行所有测试点
        List<FutureTask<JSONObject>> futureTasks = new ArrayList<>();
        for (int index = 0; index < testcaseList.size(); index++) {
            JSONObject testcase = (JSONObject) testcaseList.get(index);
            final int testCaseId = index + 1;
            final String inputFileName = testcase.getStr("inputName");
            final String outputFileName = testcase.getStr("outputName");

            CaseJudgeDTO caseJudgeDTO = CaseJudgeDTO.builder()
                    .testCaseId(testCaseId)
                    .testCaseInputFileName(inputFileName)
                    .testCaseInputPath(testCasesDir + File.separator + inputFileName)
                    .testCaseInputContent(aliOSSUtil.download(problem.getInputFileUrls().get(index)))
                    .testCaseOutputContent(aliOSSUtil.download(problem.getOutputFileUrls().get(index)))
                    .testCaseOutputFileName(outputFileName)
                    .testCaseOutputPath(testCasesDir + File.separator + inputFileName)
                    .maxOutputSize(Math.max(testcase.getLong("outputSize", 0L) * 2, 32 * 1024 * 1024L))
                    .build();

            futureTasks.add(new FutureTask<>(() -> {
                JSONObject res = judgeOneCase(caseJudgeDTO, judgeGlobalDTO);
                res.set("caseId", testCaseId);
                res.set("inputName", inputFileName);
                res.set("outputName", outputFileName);
                return res;
            }));
        }

        return SubmitBatchTask2ThreadPool(futureTasks);
    }

    /**
     * 对一个测试点进行评测，返回评测结果。执行过程如下：
     * <p> 1. 调用{@link #runOneCase}运行测试点，即向 Sandbox 发送请求
     * <p> 2. 根据 Sandbox 响应结果构建程序运行结果，包括时间、内存、状态等
     * <p> 3. 调用{@link #checkResult}生成改测试点的评测结果
     * @param caseJudgeDTO 测试点信息
     * @param globalJudgeDTO   整体评测信息
     * @return 测试点评测结果（JSON格式）
     * @throws SystemError  系统错误
     */
    private JSONObject judgeOneCase(CaseJudgeDTO caseJudgeDTO, GlobalJudgeDTO globalJudgeDTO) throws SystemError {
        JSONArray runResultList = runOneCase(caseJudgeDTO, globalJudgeDTO);

        String stdoutName = "stdout";   // 目前所有输入输出都是stdin和stdout, isFileIO 为 false
        JSONObject judgeResult = (JSONObject) runResultList.get(0);
        SandboxResult sandBoxRes = SandboxResult.builder()
                .stdout(((JSONObject) judgeResult.get("files")).getStr(stdoutName, ""))
                .stderr(((JSONObject) judgeResult.get("files")).getStr("stderr"))
                .time(judgeResult.getLong("time") / 1000000)    //  ns -> ms
                .memory(judgeResult.getLong("memory") / 1024)   //  b  -> kb
                .exitCode(judgeResult.getLong("exitStatus"))
                .status(judgeResult.getInt("status"))
                .originalStatus(judgeResult.getStr("originalStatus"))
                .build();

        return checkResult(sandBoxRes, caseJudgeDTO, globalJudgeDTO);
    }

    private JSONArray runOneCase(CaseJudgeDTO caseJudgeDTO, GlobalJudgeDTO globalJudgeDTO) throws SystemError {
        return sandboxUtil.testCase(
                parseCommand(globalJudgeDTO.getRunConfig().getRunCommand()),
                globalJudgeDTO.getRunConfig().getRunEnvs(),
                caseJudgeDTO.getTestCaseInputPath(),
                caseJudgeDTO.getTestCaseInputContent(),
                globalJudgeDTO.getTestTime(),
                globalJudgeDTO.getMaxMemory(),
                caseJudgeDTO.getMaxOutputSize(),
                globalJudgeDTO.getMaxStack(),
                globalJudgeDTO.getRunConfig().getExeName(),
                globalJudgeDTO.getUserFileId(),
                globalJudgeDTO.getUserFileContent(),
                false,
                null,
                null
        );
    }

    /**
     * 提交评测任务到线程池执行
     * @param futureTasks   评测任务
     * @return 评测结果
     * @throws InterruptedException 任务中断异常
     * @throws ExecutionException   任务执行异常
     */
    private List<JSONObject> SubmitBatchTask2ThreadPool(List<FutureTask<JSONObject>> futureTasks)
            throws InterruptedException, ExecutionException {
        for (FutureTask<JSONObject> futureTask : futureTasks) {
            ThreadPoolUtil.getInstance().getThreadPool().submit(futureTask);
        }
        List<JSONObject> result = new LinkedList<>();
        while (!futureTasks.isEmpty()) {
            Iterator<FutureTask<JSONObject>> iterable = futureTasks.iterator();
            while (iterable.hasNext()) {
                FutureTask<JSONObject> future = iterable.next();
                if (future.isDone() && !future.isCancelled()) {
                    // 获取线程返回结果
                    result.add(future.get());
                    // 在任务完成移除
                    iterable.remove();
                }
            }
        }
        return result;
    }

    private JSONObject checkResult(SandboxResult sandBoxRes, CaseJudgeDTO caseJudgeDTO, GlobalJudgeDTO globalJudgeDTO) {

        JSONObject result = new JSONObject();

        StringBuilder errMsg = new StringBuilder();
        // 如果测试运行无异常
        if (sandBoxRes.getStatus().equals(JudgeStatus.STATUS_ACCEPTED.getStatus())) {
            // 对结果的时间损耗和空间损耗与题目限制做比较，判断是否 mle 和 tle
            if (sandBoxRes.getTime() > globalJudgeDTO.getMaxTime()) {
                result.set("status", JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
            } else if (sandBoxRes.getMemory() > globalJudgeDTO.getMaxMemory() * 1024) {
                result.set("status", JudgeStatus.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus());
            } else {
                log.debug("Test case {} output: {}", caseJudgeDTO.getTestCaseId(), sandBoxRes.getStdout());
                // 与原测试数据输出的md5进行对比 AC或者是WA
                JSONObject testcaseInfo = (JSONObject) ((JSONArray) globalJudgeDTO.getTestCaseInfo().get("testCases")).get(caseJudgeDTO.getTestCaseId() - 1);
                result.set("status", compareOutput(sandBoxRes.getStdout(), globalJudgeDTO.getRemoveEOLBlank(), testcaseInfo));
            }
        } else if (sandBoxRes.getStatus().equals(JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED.getStatus())) {
            result.set("status", JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        } else if (sandBoxRes.getExitCode() != 0) {
            result.set("status", JudgeStatus.STATUS_RUNTIME_ERROR.getStatus());
            if (sandBoxRes.getExitCode() < 32) {
                errMsg.append(String.format("The program return exit status code: %s (%s)\n", sandBoxRes.getExitCode(), SandboxUtil.signals.get(sandBoxRes.getExitCode().intValue())));
            } else {
                errMsg.append(String.format("The program return exit status code: %s\n", sandBoxRes.getExitCode()));
            }
        } else {
            result.set("status", sandBoxRes.getStatus());
            // 输出超限的特别提示
            if ("Output Limit Exceeded".equals(sandBoxRes.getOriginalStatus())){
                errMsg.append("The output character length of the program exceeds the limit");
            }
        }

        // b
        result.set("memory", sandBoxRes.getMemory());
        // ns->ms
        result.set("time", sandBoxRes.getTime());

        // 记录该测试点的错误信息
        if (StringUtils.hasLength(errMsg.toString())) {
            String str = errMsg.toString();
            result.set("errMsg", str.substring(0, Math.min(1024 * 1024, str.length())));
        }

        // 如果需要获取用户对于该题目的输出
        if (globalJudgeDTO.getNeedUserOutput()) {
            result.set("output", sandBoxRes.getStdout());
        }

        result.set("finishTime", LocalDateTime.now());

        return result;
    }

    public static List<String> parseCommand(String command) {
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

    /**
     * 根据评测结果与用户程序输出的字符串MD5进行对比，判断是否通过测试点
     * @param userOutput    用户程序输出内容
     * @param isRemoveEOLBlank 是否去掉末尾空格
     * @param testcaseInfo 测试点信息，要用到关于测试点期望输出的字段：
     * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; {@code outputMd5}: 原测试数据输出的md5
     * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; {@code allStrippedOutputMd5}: 去掉所有空格的md5
     * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; {@code EOFStrippedOutputMd5}: 去掉末尾空格的md5
     * @return 用状态码代表评测结果，下面三种之一：
     * <p>{@link JudgeStatus#STATUS_ACCEPTED},
     * <p>{@link JudgeStatus#STATUS_PRESENTATION_ERROR},
     * <p>{@link JudgeStatus#STATUS_WRONG_ANSWER}
     */
    private Integer compareOutput(String userOutput, Boolean isRemoveEOLBlank, JSONObject testcaseInfo) {
        // 如果当前题目选择默认去掉字符串末位空格
        if (isRemoveEOLBlank) {
            String userOutputMd5 = DigestUtils.md5DigestAsHex(testCaseUtil.rtrim(userOutput).getBytes(StandardCharsets.UTF_8));
            if (userOutputMd5.equals(testcaseInfo.getStr("EOFStrippedOutputMd5"))) {
                return JudgeStatus.STATUS_ACCEPTED.getStatus();
            }else{
                return JudgeStatus.STATUS_WRONG_ANSWER.getStatus();
            }
        } else { // 不选择默认去掉文末空格 与原数据进行对比
            String userOutputMd5 = DigestUtils.md5DigestAsHex(userOutput.getBytes(StandardCharsets.UTF_8));
            if (userOutputMd5.equals(testcaseInfo.getStr("outputMd5"))) {
                return JudgeStatus.STATUS_ACCEPTED.getStatus();
            }
        }
        // 如果不AC,进行PE判断，否则为WA
        String userOutputMd5 = DigestUtils.md5DigestAsHex(userOutput.replaceAll("\\s+", "").getBytes(StandardCharsets.UTF_8));
        if (userOutputMd5.equals(testcaseInfo.getStr("allStrippedOutputMd5"))) {
            return JudgeStatus.STATUS_PRESENTATION_ERROR.getStatus();
        } else {
            return JudgeStatus.STATUS_WRONG_ANSWER.getStatus();
        }
    }


}
