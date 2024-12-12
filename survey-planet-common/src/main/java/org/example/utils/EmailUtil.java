package org.example.utils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Slf4j
public class EmailUtil {
    private final JavaMailSender mailSender;
    private final String email;

    public EmailUtil(JavaMailSender mailSender, String email) {
        this.mailSender = mailSender;
        this.email = email;
    }

    /**
     * Send email
     * @param to recipient
     * @param subject email subject
     * @param htmlContent email content in HTML format
     */
    @Retryable(
            value = MailSendException.class,
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public void sendEmail(String to, String subject, String htmlContent) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true表示发送HTML邮件
            helper.setFrom(email);
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
