package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.annotation.AutoFill;
import org.example.entity.message.InviteMessage;
import org.example.entity.message.Message;
import org.example.entity.message.NewSubmissionMessage;
import org.example.entity.message.SystemMessage;
import org.example.enumeration.OperationType;

import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Mapper
public interface MessageMapper {
    List<Message> getMessageByUid(Long uid);

    List<Message> getUnreadMessageByUid(Long uid);

    @AutoFill(OperationType.UPDATE)
    Long insertSystemMessage(SystemMessage message);

    @AutoFill(OperationType.INSERT)
    Long insertInviteMessage(InviteMessage message);

    @AutoFill(OperationType.INSERT)
    Long insertNewSubmissionMessage(NewSubmissionMessage message);
}
