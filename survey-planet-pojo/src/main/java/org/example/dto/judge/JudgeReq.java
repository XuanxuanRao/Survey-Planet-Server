package org.example.dto.judge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.judge.Judge;
import org.example.enumeration.JudgeTaskType;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeReq {
    /**
     * 评测任务类型：用户提交，题目发布者重测
     */
    private JudgeTaskType type;

    /**
     * 评测请求数据
     */
    private Judge judge;
}
