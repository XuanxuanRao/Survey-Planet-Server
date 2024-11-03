package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.constant.VerificationCodeConstant;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCode {
    private String email;
    private String code;
    private LocalDateTime expireTime;

    public VerificationCode(String email, String code) {
        this.email = email;
        this.code = code;
        this.expireTime = LocalDateTime.now().plusSeconds(VerificationCodeConstant.VALIDITY_PERIOD);
    }
}
