package org.example.judge;

import cn.hutool.json.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.JudgeConstant;
import org.example.entity.judge.LanguageConfig;
import org.example.entity.judge.Judge;
import org.example.entity.judge.CaseJudgeResult;
import org.example.entity.question.CodeQuestion;
import org.example.enumeration.JudgeStatus;
import org.example.exception.CompileError;
import org.example.exception.SubmitError;
import org.example.exception.SystemError;
import org.example.judge.utils.TestCaseUtil;
import org.example.utils.SandboxUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Component
@Slf4j
public class JudgeContext {

    @Resource
    private LanguageConfigLoader languageConfigLoader;

    @Resource
    private JudgeRunner judgeRunner;

    @Resource
    private JudgeCompiler JudgeCompiler;

    @Resource
    private TestCaseUtil testCaseUtil;

    @Resource
    private SandboxUtil SandboxUtil;

    /**
     * 开始评测任务，首先编译代码，得到 fileId，然后通过 fileId 执行编译得到的可执行文件，运行测试用例
     * @param problem 题目信息
     * @param judge   提交信息
     * @return 评测结果
     */
    public Judge judge(CodeQuestion problem, Judge judge) {
        // 加载语言配置
        LanguageConfig languageConfig = languageConfigLoader.getLanguageConfigByName(judge.getLanguage());
        if (languageConfig.getSrcName() == null ||
                (!languageConfig.getSrcName().endsWith(".c") && !languageConfig.getSrcName().endsWith(".cpp"))) {
            problem.setTimeLimit(problem.getTimeLimit() * 2);
            problem.setMemoryLimit(problem.getMemoryLimit() * 2);
        }

        String userFileId = null;
        try {
            // 对于不支持编译的语言(js、php)，需要提供源代码
            if (languageConfig.getCompileCommand() != null) {
                userFileId = JudgeCompiler.compile(languageConfig, judge.getCodeContent());
            }
            // 测试数据文件所在文件夹
            String testCasesDir = JudgeConstant.TEST_CASE_DIR + File.separator + "problem_" + problem.getQid();
            // 从文件中加载测试数据json
            JSONObject testCasesInfo = testCaseUtil.loadTestCasesInfo(problem.getQid(), testCasesDir);

            List<JSONObject> allCaseResultList = judgeRunner.judgeAllCase(
                    judge.getSubmitId(),    // submitId
                    problem,                // problem
                    testCasesDir,           // testCaseDir
                    judge.getLanguage(),    // language
                    testCasesInfo,          // testCasesInfo
                    true,                   // needUserOutput
                    userFileId,             // userFileId
                    judge.getCodeContent()  // userFileContent
            );
            return getJudgeInfo(allCaseResultList, problem, judge);
        } catch (SystemError systemError) {
            return handleSystemError(problem, judge, systemError);
        } catch (SubmitError submitError) {
            return handleSubmitError(problem, judge, submitError);
        } catch (CompileError compileError) {
            return handleCompileError(judge, compileError);
        } catch (Exception e) {
            log.error("[Judge] [System Runtime Error] Submit Id:[{}] Problem Id:[{}], Error:[{}]",
                    judge.getSubmitId(),
                    problem.getQid(),
                    e.getMessage());
            return Judge.builder()
                    .submitId(judge.getSubmitId())
                    .uid(judge.getUid())
                    .qid(judge.getQid())
                    .codeContent(judge.getCodeContent())
                    .language(judge.getLanguage())
                    .status(JudgeStatus.STATUS_SYSTEM_ERROR.getStatus())
                    .score(0)
                    .errorMessage("Something unknown has gone wrong. Please report this to chenxuanrao06@gmail.com or serveyplanetservice@gmail.com!")
                    .build();
        } finally {
            if (StringUtils.hasLength(userFileId)) {
                SandboxUtil.delFile(userFileId);
            }
        }
    }

    private Judge handleCompileError(Judge judge, CompileError compileError) {
        return Judge.builder()
                .submitId(judge.getSubmitId())
                .uid(judge.getUid())
                .qid(judge.getQid())
                .codeContent(judge.getCodeContent())
                .language(judge.getLanguage())
                .status(JudgeStatus.STATUS_COMPILE_ERROR.getStatus())
                .score(0)
                .errorMessage(mergeNonEmptyStrings(compileError.getStdout(), compileError.getStderr()))
                .build();
    }

    private Judge handleSubmitError(CodeQuestion problem, Judge judge, SubmitError submitError) {
        log.error("[Judge] [Submit Error] Submit Id:[{}] Problem Id:[{}], Error:[{}]",
                judge.getSubmitId(),
                problem.getQid(),
                submitError.getMessage());
        return Judge.builder()
                .submitId(judge.getSubmitId())
                .uid(judge.getUid())
                .qid(judge.getQid())
                .codeContent(judge.getCodeContent())
                .language(judge.getLanguage())
                .status(JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus())
                .score(0)
                .errorMessage(mergeNonEmptyStrings(submitError.getMessage(), submitError.getStdout(), submitError.getStderr()))
                .build();
    }

    private static Judge handleSystemError(CodeQuestion problem, Judge judge, SystemError systemError) {
        log.error("[Judge] [System Error] Submit Id:[{}] Problem Id:[{}], Error:[{}]",
                judge.getSubmitId(),
                problem.getQid(),
                systemError.getMessage());
        return Judge.builder()
                .submitId(judge.getSubmitId())
                .uid(judge.getUid())
                .qid(judge.getQid())
                .codeContent(judge.getCodeContent())
                .language(judge.getLanguage())
                .status(JudgeStatus.STATUS_SYSTEM_ERROR.getStatus())
                .score(0)
                .errorMessage("Something has gone wrong with the judgeServer. Please report this to chenxuanrao06@gmail.com or serveyplanetservice@gmail.com!")
                .build();
    }

    /**
     * 综合每个测试点的结果，将json格式的数据转为 {@link CaseJudgeResult}，并得到最终的评测结果 {@link Judge}
     * @param testCaseResultList 每个测试点的结果，来自 {@link JudgeRunner#judgeOneCase}
     * @param problem 题目
     * @param judge 提交
     * @return 最终的评测结果
     */
    public Judge getJudgeInfo(List<JSONObject> testCaseResultList,
                                          CodeQuestion problem,
                                          Judge judge) {

        Judge result = Judge.builder()
                .submitId(judge.getSubmitId())
                .uid(judge.getUid())
                .qid(problem.getQid())
                .language(judge.getLanguage())
                .codeContent(judge.getCodeContent())
                .build();

        List<JSONObject> errorTestCaseList = new LinkedList<>();

        List<CaseJudgeResult> allCaseResList = testCaseResultList.stream().map(jsonObj -> {
            Integer time = jsonObj.getLong("time").intValue();
            Integer memory = jsonObj.getLong("memory").intValue();
            Integer status = jsonObj.getInt("status");
            Long caseId = jsonObj.getLong("caseId", null);
            // String inputFileName = jsonObj.getStr("inputFileName");
            // String outputFileName = jsonObj.getStr("outputFileName");
            String userOutput = jsonObj.getStr("output");
            String errMsg = jsonObj.getStr("errMsg");
            LocalDateTime finishTime = jsonObj.get("finishTime", LocalDateTime.class);

            CaseJudgeResult judgeCase = CaseJudgeResult.builder()
                    .submitId(judge.getSubmitId())
                    .qid(problem.getQid())
                    .caseId(caseId)
                    .time(time)
                    .memory(memory)
                    .status(status)
                    .inputDataUrl(problem.getInputFileUrls().get((int) (caseId-1)))
                    .outputDataUrl(problem.getOutputFileUrls().get((int) (caseId-1)))
                    .userOutput(userOutput)
                    .status(status)
                    .createTime(finishTime)
                    .build();

            if (StringUtils.hasLength(errMsg) && !Objects.equals(status, JudgeStatus.STATUS_COMPILE_ERROR.getStatus())) {
                judgeCase.setUserOutput(errMsg);
            } else {
                judgeCase.setUserOutput(userOutput);
            }

            if (!Objects.equals(status, JudgeStatus.STATUS_ACCEPTED.getStatus())) {
                errorTestCaseList.add(jsonObj);
            }

            return judgeCase;
        }).toList();

        result.setCaseJudgeResults(allCaseResList);

        if (!errorTestCaseList.isEmpty()) {
            JSONObject errorCase = errorTestCaseList.get(0);
            result.setStatus(errorCase.getInt("status"));
            result.setErrorMessage(errorCase.getStr("errMsg"));
        } else {
            result.setStatus(JudgeStatus.STATUS_ACCEPTED.getStatus());
        }
        
        result.setScore((int) (((double)((allCaseResList.size() - errorTestCaseList.size())) / allCaseResList.size()) * problem.getScore()));

        return result;
    }

    private String mergeNonEmptyStrings(String... strings) {
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            if (StringUtils.hasLength(str)) {
                sb.append(str, 0, Math.min(1024 * 1024, str.length())).append("\n");
            }
        }
        return sb.toString();
    }
}
