package org.example.handler.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.example.entity.survey.SurveyType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class SurveyTypeHandler extends BaseTypeHandler<SurveyType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, SurveyType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getValue());
    }

    @Override
    public SurveyType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String type = rs.getString(columnName);
        return type != null ? SurveyType.fromString(type) : null;
    }

    @Override
    public SurveyType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String type = rs.getString(columnIndex);
        return type != null ? SurveyType.fromString(type) : null;
    }

    @Override
    public SurveyType getNullableResult(CallableStatement cs, int columnIndex) {
        return null;
    }
}
