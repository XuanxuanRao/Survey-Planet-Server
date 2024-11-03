package org.example.exception;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
public class SystemError extends Exception {
    private String message;
    private String stdout;
    private String stderr;

    public SystemError(String message, String stdout, String stderr) {
        super(message + " " + stderr);
        this.message = message;
        this.stdout = stdout;
        this.stderr = stderr;
    }
}
