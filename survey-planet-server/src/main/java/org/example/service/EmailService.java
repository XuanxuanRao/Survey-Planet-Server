package org.example.service;

import org.example.dto.EmailSendCodeDTO;

public interface EmailService {
    void sendVerificationCode(EmailSendCodeDTO emailSendCodeDTO);
}
