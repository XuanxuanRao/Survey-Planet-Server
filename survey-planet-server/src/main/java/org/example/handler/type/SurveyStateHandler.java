package org.example.handler.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.example.entity.survey.SurveyState;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SurveyStateHandler extends BaseTypeHandler<SurveyState> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, SurveyState parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getValue());
    }

    @Override
    public SurveyState getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String state = rs.getString(columnName);
        return state != null ? SurveyState.fromString(state) : null;
    }

    @Override
    public SurveyState getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String state = rs.getString(columnIndex);
        return state != null ? SurveyState.fromString(state) : null;
    }

    @Override
    public SurveyState getNullableResult(CallableStatement cs, int columnIndex) {
        return null;
    }
}