package org.example.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.example.result.Result;
import org.example.annotation.ControllerLog;
import org.example.dto.email.EmailSendCodeDTO;
import org.example.service.EmailService;
import org.example.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Slf4j
@RestController
@RequestMapping("/api/common")
public class CommonController {
    @Resource
    private FileService fileService;

    @Resource
    private EmailService emailService;

    @PostMapping("/upload")
    @ControllerLog(name = "upload", intoDB = true)
    public Result<String> upload(MultipartFile file) {
        try {
            String path = fileService.uploadFile(file.getBytes(), file.getOriginalFilename(), file.getSize());
            return Result.success(path);
        } catch (IOException e) {
            log.error("File upload failed", e);
        }
        return Result.error("file upload failed");
    }

    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam String fileUrl) {
        fileService.deleteFile(fileUrl);
        return Result.success();
    }

    @PostMapping("/email/code")
    @ControllerLog(name = "sendCode2Email", intoDB = true)
    public Result<String> sendCode2Email(@RequestBody EmailSendCodeDTO emailSendCodeDTO) {
        emailService.sendVerificationCode(emailSendCodeDTO);
        return Result.success("Email sent successfully");
    }
}
