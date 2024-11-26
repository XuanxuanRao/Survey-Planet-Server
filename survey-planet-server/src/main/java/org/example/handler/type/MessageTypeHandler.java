package org.example.handler.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.example.entity.message.MessageType;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
public class MessageTypeHandler extends BaseTypeHandler<MessageType> {
    @Override
    public void setNonNullParameter(java.sql.PreparedStatement ps, int i, MessageType parameter, org.apache.ibatis.type.JdbcType jdbcType) throws java.sql.SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public MessageType getNullableResult(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        Integer type = Integer.valueOf(rs.getString(columnName));
        return MessageType.of(type);
    }

    @Override
    public MessageType getNullableResult(java.sql.ResultSet rs, int columnIndex) throws java.sql.SQLException {
        Integer type = Integer.valueOf(rs.getString(columnIndex));
        return MessageType.of(type);
    }

    @Override
    public MessageType getNullableResult(java.sql.CallableStatement cs, int columnIndex) {
        return null;
    }
}
