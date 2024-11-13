package org.example.mapper;

import org.example.entity.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * @author chenxuanrao06@gmail.com
 * @Description: Repository for LogEntry.
 * <p> 目前项目中其他的数据操作通过 Mybatis 实现，这里通过 JPA 实现日志相关数据库操作
 */
@Repository
public interface LogRepository extends JpaRepository<LogEntry, Long> {
}
