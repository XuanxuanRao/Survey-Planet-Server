package org.example.service;

import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.ResponseDTO;
import org.example.entity.response.Response;
import org.example.vo.ResponseVO;

import java.io.IOException;
import java.util.List;

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
     * @param httpServletResponse HttpServletResponse, 用于存放 Excel 文件
     * @return
     */
    void export(Long sid, HttpServletResponse httpServletResponse) throws IOException;

    ResponseVO getResponseByRid(Long rid);
}
