package org.example.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.email.EmailNotifyNewSubmissionDTO;
import org.example.dto.email.EmailSendCodeDTO;
import org.example.dto.email.EmailSendInvitationDTO;
import org.example.entity.VerificationCode;
import org.example.exception.IllegalRequestException;
import org.example.service.EmailService;
import org.example.service.UserService;
import org.example.service.VerificationCodeService;
import org.example.utils.EmailUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    @Value("${spring.mail.username}")
    private String email;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private VerificationCodeService verificationCodeService;

    @Resource
    private UserService userService;

    /**
     * 注册
     */
    private static final String REGISTER = "reg";
    /**
     * 重置密码
     */
    private static final String RESET = "reset";

    @Override
    public void sendVerificationCode(EmailSendCodeDTO emailSendCodeDTO) {
        if (REGISTER.equals(emailSendCodeDTO.getType()) && userService.getByEmail(emailSendCodeDTO.getEmail()) != null) {
            throw new IllegalRequestException(
                    EmailService.class.getName() + ".sendVerificationCode()",
                    "Email already registered: " + emailSendCodeDTO.getEmail()
            );
        } else if (RESET.equals(emailSendCodeDTO.getType()) && userService.getByEmail(emailSendCodeDTO.getEmail()) == null) {
            throw new IllegalRequestException(
                    EmailService.class.getName() + ".sendVerificationCode()",
                    "Email not registered: " + emailSendCodeDTO.getEmail()
            );
        }

        String subject = "Verification code";
        String code = String.valueOf(new Random().nextInt(899999) + 100000);

        String rawContent = loadTemplateContent(switch (emailSendCodeDTO.getType()) {
            case REGISTER -> "templates/email_register.html";
            case RESET -> "templates/email_reset.html";
            default -> throw new IllegalRequestException(
                    EmailService.class.getName() + ".getHtmlContent()",
                    "Invalid email type: " + emailSendCodeDTO.getType()
            );
        });

        String htmlContent = switch (emailSendCodeDTO.getType()) {
            case REGISTER -> rawContent
                    .replace("{{verification_code}}", code);
            case RESET -> rawContent
                    .replace("{{verification_code}}", code)
                    .replace("{{username}}", userService.getByEmail(emailSendCodeDTO.getEmail()).getUsername());

            default -> rawContent;
        };

        // 使用 EmailUtil 发送邮件
        new EmailUtil(mailSender, email).sendEmail(emailSendCodeDTO.getEmail(), subject, htmlContent);

        // 将验证码保存到数据库
        verificationCodeService.insert(new VerificationCode(emailSendCodeDTO.getEmail(), code));
    }

    @Override
    public void sendInvitation(EmailSendInvitationDTO emailSendInvitationDTO) {
        String subject = "Survey Invitation";

        String rawContent = loadTemplateContent("templates/email_invite.html");
        String htmlContent = rawContent
                .replace("{{sender_name}}", emailSendInvitationDTO.getFrom())
                .replace("{{recipient_name}}", emailSendInvitationDTO.getTo())
                .replace("{{survey_type}}", emailSendInvitationDTO.getSurveyType())
                .replace("{{invitation_message}}", emailSendInvitationDTO.getInvitationMessage())
                .replace("{{survey_name}}", emailSendInvitationDTO.getSurveyName())
                .replace("{{survey_link}}", emailSendInvitationDTO.getSurveyLink());

        // 使用 EmailUtil 发送邮件
        new EmailUtil(mailSender, email).sendEmail(emailSendInvitationDTO.getEmail(), subject, htmlContent);
    }

    @Override
    public void sendNotificationForNewSubmission(EmailNotifyNewSubmissionDTO emailNotifyNewSubmissionDTO) {
        String subject = "New submission notification";

        Context context = new Context();
        context.setVariable("username", emailNotifyNewSubmissionDTO.getUsername());
        context.setVariable("surveys", emailNotifyNewSubmissionDTO.getNewSubmissionVOs());

        String htmlContent = templateEngine.process("email_new_submission", context);

        new EmailUtil(mailSender, email).sendEmail(emailNotifyNewSubmissionDTO.getEmail(), subject, htmlContent);
    }

    private String loadTemplateContent(String fileName) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            log.error("Email template {} not found", fileName);
            throw new RuntimeException("Email template not found");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Error reading email template: " + fileName, e);
        }
    }
}
