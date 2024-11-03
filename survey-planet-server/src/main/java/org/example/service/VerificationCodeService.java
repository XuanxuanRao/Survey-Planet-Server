package org.example.service;

import org.example.entity.VerificationCode;

public interface VerificationCodeService {
    void insert(VerificationCode verificationCode);

    boolean query(String email, String code);

    Integer clear();

    Integer delete(String email);
}
