package org.example.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ResponseItem {
    @EqualsAndHashCode.Include
    private Long submitId;
    private Long rid;
    private Long qid;
    private Integer grade;
    private List<String> content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
