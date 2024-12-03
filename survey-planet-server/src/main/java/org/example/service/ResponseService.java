package org.example.service;

import jakarta.servlet.http.HttpServletResponse;
import org.example.result.PageResult;
import org.example.dto.ResponseDTO;
import org.example.dto.ResponsePageQueryDTO;
import org.example.entity.response.Response;
import org.example.entity.response.ResponseItem;
import org.example.vo.ResponseVO;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenxuanrao06@gmail.com
 * @ClassName ResponseService
 * @description
 */
public interface ResponseService {

    Long submit(ResponseDTO responseDTO);

    List<Response> getResponseBySid(Long sid);

    List<Response> getResponseByUid(Long uid);

    void deleteBySid(Long sid);

    /**
     * 导出问卷的填写结果到 Excel
     * @param sid 要导出的问卷 ID
     * @param httpServletResponse 用于携带导出的 Excel 文件
     */
    void export(Long sid, HttpServletResponse httpServletResponse) throws IOException;

    ResponseVO getResponseByRid(Long rid);

    PageResult<Response> pageQuery(ResponsePageQueryDTO responsePageQueryDTO);

    List<ResponseItem> getResponseItemsByQid(Long qid);

    /**
     * 获取用户提交的历史记录
     * @param uid 用户 ID
     * @return Map表示提交记录
     */
    Map<Long, Response> querySubmitHistory(Long uid);

    /**
     * 更新用户的提交内容
     * @param rid 提交 ID
     * @param items 更新的题目回答
     */
    void updateResponse(Long rid, List<ResponseItem> items, Boolean valid);

    Response getResponseBySubmitId(Long submitId);

    List<ResponseItem> getResponseItemsBySubmitIds(List<Long> submitIds);

    List<Response> getResponseRecordsBySid(Long sid, Boolean valid);

    List<Response> getResponseRecordsBySid(Long sid);

    /**
     * 查询最近 time 分钟内的提交
     * @param time (min)
     * @return 得到的提交信息，不含 {@link ResponseItem}
     */
    List<Response> getRecentResponse(Integer time);

    /**
     * 获取问卷在 startDate 和 endDate 内每天的提交数量
     * @param sid 问卷 ID
     * @param startDate 统计开始日期
     * @param endDate 统计结束日期
     * @return 一个 {@link LinkedHashMap}记录每天的提交数量，key 为日期，value 为提交数量，按照日期升序排列
     */
    LinkedHashMap<LocalDate, Long> getResponseCountByDate(Long sid, LocalDateTime startDate, LocalDateTime endDate);

}
