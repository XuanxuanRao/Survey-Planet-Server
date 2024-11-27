package org.example.entity.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemMessage extends Message {
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Override
    public String toText() {
        return content;
    }

    @Override
    public String toText(HashMap<String, String> params) {
        return null;
    }
}

