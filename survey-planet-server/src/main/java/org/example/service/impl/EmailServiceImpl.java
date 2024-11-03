package org.example.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.EmailSendCodeDTO;
import org.example.entity.VerificationCode;
import org.example.exception.IllegalRequestException;
import org.example.service.EmailService;
import org.example.service.UserService;
import org.example.service.VerificationCodeService;
import org.example.utils.EmailUtil;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private VerificationCodeService verificationCodeService;

    @Resource
    private UserService userService;

    private static final String REGISTER = "reg";
    private static final String RESET = "reset";

    public void sendVerificationCode(EmailSendCodeDTO emailSendCodeDTO) {
        String subject = "Verification code";
        String code = String.valueOf(new Random().nextInt(899999) + 100000);

        String htmlContent = getHtmlContent(emailSendCodeDTO.getEmail(), emailSendCodeDTO.getType(), code);

        // 使用 EmailUtil 发送邮件
        new EmailUtil(mailSender).sendEmail(emailSendCodeDTO.getEmail(), subject, htmlContent);

        // 将验证码保存到数据库
        verificationCodeService.insert(new VerificationCode(emailSendCodeDTO.getEmail(), code));
    }

    private String getHtmlContent(String email, String type, String code) {
        String rawContent = loadTemplateContent(switch (type) {
            case REGISTER -> "templates/email_register.html";
            case RESET -> "templates/email_reset.html";
            default -> throw new IllegalRequestException(
                    EmailService.class.getName() + ".getHtmlContent()",
                    "Invalid email type: " + type
            );
        });

        return switch (type) {
            case REGISTER -> rawContent
                    .replace("{{verification_code}}", code);
            case RESET -> rawContent
                    .replace("{{verification_code}}", code)
                    .replace("{{username}}", userService.getByEmail(email).getUsername());
            default -> rawContent;
        };
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
