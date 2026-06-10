package com.tourism.api.controller;

import com.tourism.api.config.UploadConfig;
import com.tourism.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 *
 */
@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final UploadConfig uploadConfig;

    /**
     * 上传单个图片
     */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = saveFile(file);
            return Result.success(url);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 批量上传图片
     */
    @PostMapping("/images")
    public Result<List<String>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                String url = saveFile(file);
                urls.add(url);
            }
            return Result.success(urls);
        } catch (Exception e) {
            log.error("批量上传失败", e);
            return Result.error("批量上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传视频
     */
    @PostMapping("/video")
    public Result<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            String url = saveVideoFile(file);
            return Result.success(url);
        } catch (Exception e) {
            log.error("视频上传失败", e);
            return Result.error("视频上传失败：" + e.getMessage());
        }
    }

    /**
     * 保存文件
     */
    private String saveFile(MultipartFile file) throws IOException {
        // 校验文件
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 校验文件大小
        if (file.getSize() > uploadConfig.getMaxSize()) {
            throw new IllegalArgumentException("文件大小超过限制");
        }

        // 获取原始文件名和扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }

        // 校验文件类型
        String[] allowedTypes = uploadConfig.getAllowedTypes().split(",");
        boolean isAllowed = false;
        for (String type : allowedTypes) {
            if (type.trim().equalsIgnoreCase(extension)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            throw new IllegalArgumentException("不支持的文件类型：" + extension);
        }

        // 生成文件名：日期/UUID.扩展名
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String relativePath = date + "/" + filename;

        // 创建目录
        File dir = new File(uploadConfig.getPath() + date);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        File destFile = new File(uploadConfig.getPath() + relativePath);
        file.transferTo(destFile);

        // 返回访问URL
        return uploadConfig.getUrlPrefix() + relativePath;
    }

    /**
     * 保存视频文件
     */
    private String saveVideoFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > uploadConfig.getVideoMaxSize()) {
            throw new IllegalArgumentException("视频大小超过限制（最大100MB）");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }

        String[] allowedTypes = uploadConfig.getVideoAllowedTypes().split(",");
        boolean isAllowed = false;
        for (String type : allowedTypes) {
            if (type.trim().equalsIgnoreCase(extension)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            throw new IllegalArgumentException("不支持的视频类型：" + extension);
        }

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String relativePath = "video/" + date + "/" + filename;

        File dir = new File(uploadConfig.getPath() + "video/" + date);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File destFile = new File(uploadConfig.getPath() + relativePath);
        file.transferTo(destFile);

        return uploadConfig.getUrlPrefix() + relativePath;
    }
}
