package org.example.dto.judge;

import cn.hutool.json.JSONObject;
import lombok.Builder;
import lombok.Data;
import org.example.entity.judge.LanguageConfig;

/**
 * @author chenxuanrao06@gmail.com
 * @Description: 传递评测信息，完成一次完整的评测，包含多个测试点运行所需信息。
 * <p>可以支持更多类型的评测方式拓展
 */
@Data
@Builder
public class GlobalJudgeDTO {
    /**
     * 题目 id
     */
    private Long qid;

    /**
     * 用户程序在沙盒编译后对应内存文件的id，运行时需要传入
     */
    private String userFileId;

    /**
     * 用户程序代码文件的内容
     */
    private String userFileContent;

    /**
     * 整个评测的工作目录
     */
    private String runDir;

    /**
     * Sandbox最大运行时间，为题目限制时间 + {@link org.example.constant.JudgeConstant#COMPILE_TIME} ms
     */
    private Long testTime;

    /**
     * 当前题目评测的最大时间限制 ms
     */
    private Long maxTime;

    /**
     * 当前题目评测的最大空间限制 mb
     */
    private Long maxMemory;

    /**
     * 当前题目评测的最大栈空间限制 mb
     */
    private Integer maxStack;

    /**
     * 是否需要用户输出
     */
    private Boolean needUserOutput;

    /**
     * 评测数据json内容
     */
    private JSONObject testCaseInfo;

    /**
     * 进行评测的命令配置
     */
    private LanguageConfig runConfig;

    /**
     * 是否需要自动移除评测数据的行末空格
     */
    private Boolean removeEOLBlank;
}
