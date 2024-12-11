package org.example.entity.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.dto.QuestionDTO;
import org.example.entity.response.ResponseItem;
import org.example.vo.QuestionAnalyseVO;
import org.springframework.beans.BeanUtils;

import java.util.HashMap;
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

    @Override
    public QuestionAnalyseVO analyse(List<ResponseItem> responseItems) {
        QuestionAnalyseVO questionAnalyseVO = new QuestionAnalyseVO();
        questionAnalyseVO.setQid(this.getQid());

        long total = 0;
        HashMap<Integer, Long> gradeCount = new HashMap<>();
        for (ResponseItem responseItem : responseItems) {
            if (responseItem.getContent() == null || responseItem.getContent().isEmpty()) {
                continue;
            }
            total++;
            if (responseItem.getGrade() != null) {
                gradeCount.put(responseItem.getGrade(), gradeCount.getOrDefault(responseItem.getGrade(), 0L) + 1);
            }
        }
        questionAnalyseVO.setTotal(total);
        questionAnalyseVO.setGradeCount(gradeCount);
        return questionAnalyseVO;
    }

    @Override
    public QuestionDTO toQuestionDTO() {
        var res = super.toQuestionDTO();
        BeanUtils.copyProperties(this, res);
        return res;
    }
}
