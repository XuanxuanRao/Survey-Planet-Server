package org.example.dto.judge;

import lombok.Builder;
import lombok.Data;

/**
 * @author chenxuanrao06@gmail.com
 * @Description: 记录 go-judge 沙盒的一次运行结果
 */
@Data
@Builder
public class SandboxResult {
    /**
     * 单个程序的状态码
     */
    private Integer status;

    /**
     * 原沙盒输出的状态字符
     */
    private String originalStatus;

    /**
     * 单个程序的退出码
     */
    private Long exitCode;

    /**
     * 单个程序的运行所耗空间 kb
     */
    private Long memory;

    /**
     * 单个程序的运行所耗时间 ms
     */
    private Long time;

    /**
     * 单个程序的标准输出
     */
    private String stdout;

    /**
     * 单个程序的错误信息
     */
    private String stderr;
}
