package org.example.entity.judge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseJudgeResult {
    /**
     * 题目id
     */
    private Long qid;

    /**
     * 提交的id
     */
    private Long submitId;

    /**
     * 测试样例id
     */
    private Long caseId;

    /**
     * 运行该样例所用时间ms
     */
    private Integer time;

    /**
     * 运行该样例所用空间KB
     */
    private Integer memory;

    /**
     * 测试该样例结果状态码
     */
    private Integer status;

    /**
     * 样例输入文件的OSS地址
     */
    private String inputDataUrl;

    /**
     * 样例输出文件的OSS地址
     */
    private String outputDataUrl;

    /**
     * 用户输出
     */
    private String userOutput;

    /**
     * 测试完成时间
     */
    private LocalDateTime createTime;
}
