package org.example.entity.message;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;


/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewSubmissionMessage extends Message {
    private Long sid;
    private Long rid;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Override
    public String toText() {
        return "Your survey has received new submissions!";
    }

    @Override
    public String toText(HashMap<String, String> params) {
        return null;
    }
}
