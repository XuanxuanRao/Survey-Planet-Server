package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.annotation.AutoFill;
import org.example.entity.response.Response;
import org.example.entity.response.ResponseItem;
import org.example.enumeration.OperationType;

import java.util.List;


/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Mapper
public interface ResponseMapper {

    @AutoFill(value = OperationType.INSERT)
    void insertRecord(Response response);

    void insertItems(List<ResponseItem> items);

    /**
     * 获取问卷的填写结果
     * @param sid 问卷 ID
     * @return 问卷的填写结果列表
     */
    List<Response> getBySid(Long sid);

    void deleteRecordsBySid(Long sid);

    void deleteRecordByRid(Long rid);

    void deleteItemsByRid(Long rid);

    List<Response> getByUid(Long uid);

    Response getByRid(Long rid);

    void setRecordGrade(Long rid, Integer grade);

    void setItemGrade(Long submitId, Integer grade);
}
