package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponsePageQueryDTO {
    private Integer pageNum;
    private Integer pageSize;
    private Boolean valid;
    private Long sid;
    private Integer gradeLb;
    private Integer gradeUb;
    /**
     * 查询条件
     * <p> key: 问题 id
     * <p> value: 问题的回答内容
     */
    private Map<Long, String> queryMap;
}
