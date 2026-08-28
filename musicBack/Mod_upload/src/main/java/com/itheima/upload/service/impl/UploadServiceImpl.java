package com.itheima.upload.service.impl;

import com.itheima.upload.dto.UploadResult;
import com.itheima.upload.service.UploadService;
import com.itheima.upload.util.AudioDurationUtil;
import com.itheima.upload.util.ResourcePathResolver;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

@Service
public class UploadServiceImpl implements UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadServiceImpl.class);

    @Value("${upload.image.allowed-extensions}")
    private String[] imageAllowedExtensions;

    @Value("${upload.image.allowed-mime-types}")
    private String[] imageAllowedMimeTypes;

    @Value("${upload.image.max-size}")
    private long imageMaxSize;

    @Value("${upload.lrc.allowed-extensions}")
    private String[] lrcAllowedExtensions;

    @Value("${upload.lrc.allowed-mime-types}")
    private String[] lrcAllowedMimeTypes;

    @Value("${upload.lrc.max-size}")
    private long lrcMaxSize;

    @Value("${upload.music.allowed-extensions}")
    private String[] musicAllowedExtensions;

    @Value("${upload.music.allowed-mime-types}")
    private String[] musicAllowedMimeTypes;

    @Value("${upload.music.max-size}")
    private long musicMaxSize;

    @Override
    public UploadResult uploadImage(MultipartFile file) {
        return uploadFile(file, ResourcePathResolver.resolveDir(ResourcePathResolver.IMAGE),
                imageAllowedExtensions, imageAllowedMimeTypes, imageMaxSize, "image");
    }

    @Override
    public UploadResult uploadLrc(MultipartFile file) {
        return uploadFile(file, ResourcePathResolver.resolveDir(ResourcePathResolver.LRC),
                lrcAllowedExtensions, lrcAllowedMimeTypes, lrcMaxSize, "lrc");
    }

    @Override
    public UploadResult uploadMusic(MultipartFile file) {
        return uploadFile(file, ResourcePathResolver.resolveDir(ResourcePathResolver.MUSIC),
                musicAllowedExtensions, musicAllowedMimeTypes, musicMaxSize, "music");
    }

    @Override
    public void downloadMusic(String fileUrl, HttpServletResponse response) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("文件URL不能为空");
        }

        String filePath = ResourcePathResolver.resolveDir(ResourcePathResolver.MUSIC) + fileUrl.replace("/uploads/music/", "");
        File file = new File(filePath);

        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }

        String fileName = file.getName();
        response.setContentType("audio/mpeg");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        response.setContentLengthLong(file.length());

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
            log.info("歌曲下载成功: {}", fileName);
        } catch (IOException e) {
            log.error("歌曲下载失败", e);
            throw new RuntimeException("歌曲下载失败: " + e.getMessage());
        }
    }

    private UploadResult uploadFile(MultipartFile file, String basePath,
                                     String[] allowedExtensions, String[] allowedMimeTypes,
                                     long maxSize, String fileType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        try {
            basePath = new File(basePath).getCanonicalPath() + "/";
        } catch (IOException e) {
            throw new RuntimeException("无法解析上传路径: " + basePath, e);
        }

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制，最大允许" + (maxSize / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

        // 双保险校验：扩展名白名单 OR MIME type 白名单，任一命中即通过
        boolean extOk = !extension.isEmpty() && Arrays.asList(allowedExtensions).contains(extension);
        boolean mimeOk = !contentType.isEmpty() && Arrays.asList(allowedMimeTypes).contains(contentType);

        if (!extOk && !mimeOk) {
            throw new IllegalArgumentException("不支持的文件类型，允许扩展名: "
                    + Arrays.toString(allowedExtensions)
                    + "，允许MIME: " + Arrays.toString(allowedMimeTypes));
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String dirPath = basePath + datePath;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = dirPath + "/" + fileName;
        try {
            file.transferTo(new File(filePath));
            log.info("文件上传成功: {}", filePath);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }

        String fileUrl = "/uploads/" + fileType + "/" + datePath + "/" + fileName;

        UploadResult result = new UploadResult();
        result.setFileName(fileName);
        result.setFilePath(filePath);
        result.setFileUrl(fileUrl);
        result.setFileSize(file.getSize());
        result.setFileType(file.getContentType());

        if ("music".equals(fileType)) {
            Integer duration = AudioDurationUtil.getDuration(Paths.get(filePath));
            result.setDuration(duration);
            if (duration != null) {
                log.info("音频时长解析成功: {} 秒", duration);
            }
        }
        return result;
    }
}