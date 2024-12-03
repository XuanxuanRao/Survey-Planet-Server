package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.annotation.AutoFill;
import org.example.entity.response.Response;
import org.example.entity.response.ResponseItem;
import org.example.enumeration.OperationType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;


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

    List<ResponseItem> getByQid(Long qid);

    List<Response> getSidByUid(Long uid);

    @AutoFill(value = OperationType.UPDATE)
    void updateResponse(Response response);

    void updateItems(List<ResponseItem> items);

    Response getBySubmitId(Long submitId);

    List<ResponseItem> getBySubmitIds(List<Long> submitIds);

    List<Response> getRecordsBySid(Long sid, Boolean valid);

    List<Long> condQuery(Long sid, Boolean valid, Integer gradeLb, Integer gradeUb, @Param("condition")Map<Long, String> queryMap, int querySize);

    List<Response> getByRids(List<Long> rids);

    List<Response> findByCreateTimeRange(LocalDateTime start, LocalDateTime end);

    @MapKey("day")
    Map<Date, Object> countDailyResponse(Long sid, LocalDateTime start, LocalDateTime end);
}
