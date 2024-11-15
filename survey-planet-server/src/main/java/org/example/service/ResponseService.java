package org.example.service;

import jakarta.servlet.http.HttpServletResponse;
import org.example.Result.PageResult;
import org.example.dto.ResponseDTO;
import org.example.dto.ResponsePageQueryDTO;
import org.example.entity.response.Response;
import org.example.entity.response.ResponseItem;
import org.example.vo.ResponseVO;

import java.io.IOException;
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
     * @param needScoring 是否需要重新计算分数
     */
    void updateResponse(Long rid, List<ResponseItem> items);

    Response getResponseBySubmitId(Long submitId);
}
