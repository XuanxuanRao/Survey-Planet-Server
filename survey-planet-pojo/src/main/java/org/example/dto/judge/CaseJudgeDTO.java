package org.example.dto.judge;

import lombok.Builder;
import lombok.Data;


/**
 * @author chenxuanrao06@gmail.com
 * @Description: 单个 testcase 评测信息传输对象
 */
@Data
@Builder
public class CaseJudgeDTO {
    /**
     * 当前题目评测点的的编号
     */
    private Integer testCaseId;

    /**
     * 当前题目评测点的输入文件的名字
     */
    private String testCaseInputFileName;

    /**
     * 当前题目评测点的输入文件的绝对路径
     */
    private String testCaseInputPath;

    /**
     * 当前题目评测点的输入内容
     */
    private String testCaseInputContent;

    /**
     * 当前题目评测点的输出文件的名字
     */
    private String testCaseOutputFileName;

    /**
     * 当前题目评测点的输出文件的绝对路径
     */
    private String testCaseOutputPath;

    /**
     * 当前题目评测点的标准输出内容
     */
    private String testCaseOutputContent;

    /**
     * 当前题目评测点的输出字符大小限制 (B)
     */
    private Long maxOutputSize;
}