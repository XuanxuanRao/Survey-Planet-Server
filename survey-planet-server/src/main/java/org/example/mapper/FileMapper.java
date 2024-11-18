package org.example.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FileMapper {

    @Insert("insert into file(name, path, size, uid, create_time) values(#{fileName}, #{fileUrl}, #{fileSize}, #{uid}, now())")
    void insert(String fileName, String fileUrl, Long fileSize, Long uid);

    @Select("select name from file where path = #{fileUrl}")
    String getFileNameByUrl(String fileUrl);

    @Delete("delete from file where path = #{fileUrl}")
    void delete(String fileUrl);

}
