package org.example.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseVO {
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
    private List<ResponseItemVO> items;
    /**
     * 得分
     */
    private Integer grade;
    /**
     * 是否完成批改
     */
    private Boolean finished;
    private String type;
}
