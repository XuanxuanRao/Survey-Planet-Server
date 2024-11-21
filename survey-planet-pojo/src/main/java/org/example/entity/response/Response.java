package org.example.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.judge.Judge;
import org.example.entity.survey.Survey;
import org.example.vo.ResponseItemVO;
import org.example.vo.ResponseVO;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    /**
     * 作答id, 作答的唯一标识, Primary Key in DB
     */
    private Long rid;
    /**
     * 回答的问卷 id
     */
    private Long sid;
    /**
     * 回答用户 id
     */
    private Long uid;
    /**
     * 首次回答时间（提交时间）
     */
    private LocalDateTime createTime;
    /**
     * 最近一次回答时间（提交时间）
     */
    private LocalDateTime updateTime;
    /**
     * 回答内容
     */
    private List<ResponseItem> items;
    /**
     * 得分
     */
    private Integer grade;
    /**
     * 是否完成批改
     */
    private Boolean finished;
    /**
     * 是否有效
     */
    private Boolean valid;

    /**
     * 将 Response 转换为 ResponseVO
     * @param itemsVO ResponseItemVO 列表
     * @param showAnswer 是否显示答案
     * @return 转化成的ResponseVO
     */
    public ResponseVO toVO(List<ResponseItemVO> itemsVO, boolean showAnswer) {
        ResponseVO responseVO = new ResponseVO();
        BeanUtils.copyProperties(this, responseVO);
        responseVO.setItems(itemsVO);
        if (!showAnswer) {
            responseVO.getItems().forEach(item -> {
                item.setAnswer(null);
                Judge judge = item.getJudge();
                if (judge != null) {
                    judge.getCaseJudgeResults().forEach(caseJudgeResult -> {
                        caseJudgeResult.setUserOutput(null);
                        caseJudgeResult.setInputDataUrl(null);
                        caseJudgeResult.setOutputDataUrl(null);
                    });
                }
            });
        }
        return responseVO;
    }
}
