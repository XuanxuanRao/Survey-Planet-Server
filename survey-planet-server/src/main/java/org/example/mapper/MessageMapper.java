package org.example.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.example.annotation.AutoFill;
import org.example.entity.message.*;
import org.example.enumeration.OperationType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Mapper
public interface MessageMapper {

    Message getMessageByMid(Long mid);

    List<Message> getMessageByUid(Long uid, Boolean isRead, MessageType type);

    @AutoFill(OperationType.INSERT)
    Long insertSystemMessage(SystemMessage message);

    @AutoFill(OperationType.INSERT)
    Long insertInviteMessage(InviteMessage message);

    @AutoFill(OperationType.INSERT)
    Long insertNewSubmissionMessage(NewSubmissionMessage message);

    @Update("update message set is_read = 1, update_time = now() where mid = #{mid}")
    void setRead(Long mid);

    @Update("update message set is_read = 0, update_time = now() where mid = #{mid}")
    void setUnread(Long mid);

    List<Message> getMessages(LocalDateTime startTime, LocalDateTime endTime, MessageType type);

    @Delete("delete from message where mid = #{mid}")
    void deleteMessage(Long mid);
}
