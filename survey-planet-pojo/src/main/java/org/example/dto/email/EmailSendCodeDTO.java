package org.example.dto.email;

import lombok.Data;

@Data
public class EmailSendCodeDTO {
    private String email;
    private String type;
}
