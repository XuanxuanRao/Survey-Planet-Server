package org.example.service.impl;

import jakarta.annotation.Resource;

import org.example.entity.VerificationCode;
import org.example.mapper.VerificationCodeMapper;
import org.example.service.VerificationCodeService;
import org.springframework.stereotype.Service;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {
    @Resource
    private VerificationCodeMapper verificationCodeMapper;

    @Override
    public void insert(VerificationCode verificationCode) {
        verificationCodeMapper.insert(verificationCode);
    }

    @Override
    public boolean query(String email, String code) {
        return verificationCodeMapper.query(email, code) > 0;
    }

    @Override
    public Integer clear() {
        return verificationCodeMapper.clear();
    }

    @Override
    public Integer delete(String email) {
        return verificationCodeMapper.delete(email);
    }
}
