package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.annotation.AutoFill;
import org.example.entity.survey.Survey;
import org.example.enumeration.OperationType;

import java.util.List;


@Mapper
public interface SurveyMapper {

    Survey getBySid(Long sid);

    /**
     * Get the list of surveys created by the user whose id is {@code userId},
     * and sort the list by keyword {@code orderBy}.
     * @param userId the id of the user
     * @param orderBy the keyword to sort the list
     * @return the list of sorted surveys
     */
    List<Survey> getCreatedList(Long userId, String orderBy);

    @AutoFill(value = OperationType.INSERT)
    void insert(Survey survey);

    @AutoFill(value = OperationType.UPDATE)
    void update(Survey survey);

    List<Survey> getDeletedList();

    /**
     * Delete surveys by id.
     * <p> It will remove the surveys from the database!
     * @param ids the list of ids of surveys to delete
     */
    Integer delete(List<Long> ids);

    /**
     * 根据问卷 ID 列表获取问卷列表
     * @param sids    问卷 ID 列表
     * @param orderBy 排序关键字（降序）
     * @return  查询得到的问卷列表
     */
    List<Survey> list(List<Long> sids, String orderBy);

    /**
     * 填写次数加一
     * @param sid
     */
    void addFillNum(Long sid);
}
