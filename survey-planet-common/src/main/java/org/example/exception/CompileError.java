package org.example.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CompileError extends Exception {
    private String message;
    private String stdout;
    private String stderr;

    public CompileError(String message, String stdout, String stderr) {
        super(message);
        this.message = message;
        this.stdout = stdout;
        this.stderr = stderr;
    }
}