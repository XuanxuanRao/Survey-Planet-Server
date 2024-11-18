package org.example.service.impl;

import org.apache.commons.lang3.tuple.Pair;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.context.BaseContext;
import org.example.mapper.FileMapper;
import org.example.service.FileService;
import org.example.utils.AliOSSUtil;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {
    @Resource
    private AliOSSUtil aliOssUtil;

    @Resource
    private FileMapper fileMapper;

    private final RestTemplate restTemplate;

    public FileServiceImpl() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(20000);
        requestFactory.setReadTimeout(180000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    @Override
    public String uploadFile(byte[] bytes, String fileName, Long fileSize) {
        String extension = fileName.substring(fileName.lastIndexOf("."));
        // 构造新文件名称
        String objectName = UUID.randomUUID() + extension;
        // 文件的请求路径
        String path = aliOssUtil.upload(bytes, objectName);

        fileMapper.insert(fileName, path, fileSize, BaseContext.getCurrentId());
        return path;
    }

    @Override
    public void deleteFile(String fileUrl) {
        aliOssUtil.delete(fileUrl);
        fileMapper.delete(fileUrl);
    }

    @Override
    public void downloadAndZipFiles(List<String> urls, String zipFileName, HttpServletResponse httpServletResponse) {
        // 设置响应的内容类型和附件下载头
        httpServletResponse.setContentType("application/zip");
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");

        final int maxConcurrency = Math.min(urls.size() / 2 + 1, 10);
        ExecutorService executor = Executors.newFixedThreadPool(maxConcurrency); // 定义线程池
        List<CompletableFuture<Pair<String, byte[]>>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < urls.size(); i++) {
                int index = i;
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        byte[] fileBytes = restTemplate.getForObject(urls.get(index), byte[].class);
                        if (fileBytes != null) {
                            String fileName = index + 1 + "_" +
                                    Optional.ofNullable(fileMapper.getFileNameByUrl(urls.get(index)))
                                            .orElseGet(() -> StringUtils.getFilename(urls.get(index)));
                            log.info("Downloaded file: " + fileName);
                            return Pair.of(fileName, fileBytes);
                        }
                    } catch (Exception e) {
                        log.error("Failed to download file: " + urls.get(index), e);
                    }
                    return null; // 返回空表示下载失败
                }, executor));
            }

            // 等待所有下载任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 写入 ZIP 流
            try (ZipOutputStream zipOut = new ZipOutputStream(httpServletResponse.getOutputStream())) {
                for (CompletableFuture<Pair<String, byte[]>> future : futures) {
                    Pair<String, byte[]> result = future.get(); // 获取结果
                    if (result != null && result.getRight() != null) {
                        zipOut.putNextEntry(new ZipEntry(result.getLeft()));
                        zipOut.write(result.getRight());
                        zipOut.closeEntry();
                    }
                }
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            log.error("Error occurred while generating ZIP file", e);
        } finally {
            executor.shutdown(); // 确保线程池关闭
        }
    }
}
