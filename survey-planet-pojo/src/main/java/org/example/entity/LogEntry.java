package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Data
@Entity
@Table(name = "log_entry")
public class LogEntry {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", length = 127)
    private String url;

    @Column(name = "uri", length = 63)
    private String uri;

    @Column(name = "http_method", length = 15)
    private String httpMethod;

    @Column(name = "ip", length = 127)
    private String ip;

    @Column(name = "class_method")
    private String classMethod;

    @Column(name = "args", length = 1023)
    private String args;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "result", length = 8191)
    private String result;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "take_time")
    private Long takeTime;
}