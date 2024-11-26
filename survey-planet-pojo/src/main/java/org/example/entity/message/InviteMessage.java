package org.example.entity.message;

import lombok.*;

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
@Builder
public class InviteMessage extends Message {
    private Long senderId;
    private Long sid;
    private String invitationMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Override
    public String toText() {
        return "You are invited to participate in a survey!";
    }

    @Override
    public String toText(HashMap<String, String> params) {
        return null;
    }
}
