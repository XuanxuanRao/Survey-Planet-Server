package org.example.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseItem {
    private Long submitId;
    private Long rid;
    private Long qid;
    private Integer grade;
    private List<String> content;
}
