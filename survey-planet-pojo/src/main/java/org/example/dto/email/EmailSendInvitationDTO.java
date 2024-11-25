package org.example.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenxuanrao06@gmail.com
 * @Description: 问卷填写邀请邮件发送DTO
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendInvitationDTO {
    /**
     * 发送方
     */
    private String from;
    /**
     * 接收方
     */
    private String to;
    /**
     * 问卷名
     */
    private String surveyName;
    /**
     * 问卷链接
     */
    private String surveyLink;
    private String surveyType;
    /**
     * 邀请信息
     */
    private String invitationMessage;
    private String email;
}
