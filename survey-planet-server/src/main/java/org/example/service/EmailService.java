package org.example.service;

import org.example.dto.email.EmailNotifyNewSubmissionDTO;
import org.example.dto.email.EmailSendCodeDTO;
import org.example.dto.email.EmailSendInvitationDTO;

public interface EmailService {
    void sendVerificationCode(EmailSendCodeDTO emailSendCodeDTO);

    void sendInvitation(EmailSendInvitationDTO emailSendInvitationDTO);

    void sendNotificationForNewSubmission(EmailNotifyNewSubmissionDTO emailNotifyNewSubmissionDTO);

}
