package org.example.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.message.MessageType;
import org.example.entity.survey.SurveyType;

import java.time.LocalDateTime;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageVO {
    private Long mid;
    private MessageType type;
    private Boolean isRead;
    private String surveyTitle;
    private SurveyType surveyType;
    private String surveyFillLink;
    private String invitationMessage;
    private String senderName;
    private LocalDateTime createTime;
    private LocalDateTime submitTime;
    private String surveyReportLink;
    private String submitLink;
}
