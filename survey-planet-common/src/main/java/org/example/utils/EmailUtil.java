package org.example.utils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;

@Slf4j
public class EmailUtil {
    private final JavaMailSender mailSender;

    public EmailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send email
     * @param to recipient
     * @param subject email subject
     * @param htmlContent email content in HTML format
     */
    public void sendEmail(String to, String subject, String htmlContent) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true表示发送HTML邮件
            mailSender.send(message);
            log.info("Sent email to {} successfully", to);
        } catch (MailAuthenticationException e) {
            log.error("Email authentication failed: {}", e.getMessage());
            throw new RuntimeException("Authentication failed while sending email");
        } catch (MailSendException e) {
            log.error("Failed to send email to {}. Error: {}", to, e.getMessage());
            throw new RuntimeException("Error occurred while sending email");
        } catch (Exception e) {
            log.error("Unexpected error while sending email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Unexpected error occurred while sending email");
        }
    }

}
