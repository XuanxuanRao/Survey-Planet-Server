package org.example.handler.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.example.entity.question.QuestionType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QuestionTypeHandler extends BaseTypeHandler<QuestionType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, QuestionType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getValue());
    }

    @Override
    public QuestionType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String type = rs.getString(columnName);
        return type != null ? QuestionType.fromString(type) : null;
    }

    @Override
    public QuestionType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String type = rs.getString(columnIndex);
        return type != null ? QuestionType.fromString(type) : null;
    }

    @Override
    public QuestionType getNullableResult(CallableStatement cs, int columnIndex) {
        return null;
    }
}
