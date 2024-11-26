package org.example.dto.survey;

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
public class ShareSurveyDTO {
    private List<String> emails;
    private String invitationMessage;
    private boolean needSiteNotification;
}
