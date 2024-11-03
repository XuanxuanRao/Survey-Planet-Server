package org.example.judge;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import jakarta.annotation.Resource;
import org.example.entity.judge.LanguageConfig;
import org.example.enumeration.JudgeStatus;
import org.example.exception.CompileError;
import org.example.exception.SubmitError;
import org.example.exception.SystemError;
import org.example.utils.SandboxUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Component
public class JudgeCompiler {

    @Resource
    private SandboxUtil sandboxUtil;

    /**
     * 编译代码
     * @param languageConfig 语言配置
     * @param code  源代码
     * @return 编译后产生可执行程序的 fileId
     * @throws SystemError  系统错误
     * @throws CompileError 编译错误
     * @throws SubmitError  提交错误
     */
    public String compile(LanguageConfig languageConfig, String code) throws SystemError, CompileError, SubmitError {

        // 调用安全沙箱进行编译
        JSONArray result = sandboxUtil.compile(languageConfig.getMaxCpuTime(),
                languageConfig.getMaxRealTime(),
                languageConfig.getMaxMemory(),
                256 * 1024 * 1024L,
                languageConfig.getSrcName(),
                languageConfig.getExeName(),
                JudgeRunner.parseCommand(languageConfig.getCompileCommand()),
                languageConfig.getCompileEnvs(),
                code,
                true,
                false,
                null
        );
        JSONObject compileResult = (JSONObject) result.get(0);
        if (compileResult.getInt("status").intValue() != JudgeStatus.STATUS_ACCEPTED.getStatus()) {
            throw new CompileError(
                    "Compile Error.",
                    ((JSONObject) compileResult.get("files")).getStr("stdout"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr")
            );
        }

        String fileId = ((JSONObject) compileResult.get("fileIds")).getStr(languageConfig.getExeName());
        if (!StringUtils.hasLength(fileId)) {
            throw new SubmitError(
                    "Executable file not found.",
                    ((JSONObject) compileResult.get("files")).getStr("stdout"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr")
            );
        }
        return fileId;
    }

}
