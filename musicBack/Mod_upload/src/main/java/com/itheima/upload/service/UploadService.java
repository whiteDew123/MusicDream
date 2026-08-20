package com.itheima.upload.service;

import com.itheima.upload.dto.UploadResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    UploadResult uploadImage(MultipartFile file);
    UploadResult uploadLrc(MultipartFile file);
    UploadResult uploadMusic(MultipartFile file);
    void downloadMusic(String fileUrl, HttpServletResponse response);
}