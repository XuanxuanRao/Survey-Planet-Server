package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.annotation.AutoFill;
import org.example.entity.User;
import org.example.enumeration.OperationType;


@Mapper
public interface UserMapper {
    @Select("select * from user where uid = #{uid}")
    User getById(Long uid);

    @Select("select * from user where username = #{username}")
    User getByUsername(String username);

    @Select("select * from user where email = #{email}")
    User getByEmail(String email);

    @Insert("insert into user(username, password, email, create_time) " +
            "values(#{username}, #{password}, #{email}, #{createTime})")
    @AutoFill(value = OperationType.INSERT)
    void insert(User user);

    @AutoFill(value = OperationType.UPDATE)
    void update(User user);
}
