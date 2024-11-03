package org.example.entity.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeQuestion extends Question {
    /**
     * 时间限制(ms)
     */
    private Integer timeLimit;

    /**
     * 内存限制(MB)
     */
    private Integer memoryLimit;

    /**
     * 栈空间限制(MB)
     */
    private Integer stackLimit;

    /**
     * 是否默认去除用户代码的每行末尾空白符
     */
    private Boolean isRemoveEndBlank;

    /**
     * 输入数据在 OSS 上的路径
     */
    private List<String> inputFileUrls;

    /**
     * 输出数据在 OSS 上的路径
     */
    private List<String> outputFileUrls;

    /**
     * 可以使用的语言
     */
    private List<String> languages;
}
