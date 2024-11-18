package org.example.service;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface FileService {

    String uploadFile(byte[] bytes, String fileName, Long fileSize);

    void deleteFile(String fileUrl);

    /**
     * 下载文件并将其打包为一个 ZIP 文件，直接通过 HTTP 响应返回。
     *
     * @param urls          文件 URL 列表
     * @param zipFileName   生成的 ZIP 文件名
     * @param httpServletResponse      HTTP 响应
     */
    void downloadAndZipFiles(List<String> urls, String zipFileName, HttpServletResponse httpServletResponse);
}
