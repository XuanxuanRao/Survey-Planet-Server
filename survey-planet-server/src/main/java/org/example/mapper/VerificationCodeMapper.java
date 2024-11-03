package org.example.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.VerificationCode;


@Mapper
public interface VerificationCodeMapper {
    @Insert("insert into verification_code(email, code, expire_time) values(#{email}, #{code}, #{expireTime})")
    void insert(VerificationCode verificationCode);

    @Select("select count(0) from verification_code where email = #{email} and code = #{code}")
    Integer query(String email, String code);

    @Delete("delete from verification_code where email = #{email}")
    Integer delete(String email);

    @Delete("delete from verification_code where now() > expire_time")
    Integer clear();
}
