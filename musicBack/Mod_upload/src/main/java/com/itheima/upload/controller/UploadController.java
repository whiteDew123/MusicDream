package com.itheima.upload.controller;

import com.itheima.domain.common.Result;
import com.itheima.upload.dto.UploadResult;
import com.itheima.upload.service.UploadService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/image")
    public Result<UploadResult> uploadImage(@RequestParam("file") MultipartFile file) {
        UploadResult result = uploadService.uploadImage(file);
        return Result.success("图片上传成功", result);
    }

    @PostMapping("/lrc")
    public Result<UploadResult> uploadLrc(@RequestParam("file") MultipartFile file) {
        UploadResult result = uploadService.uploadLrc(file);
        return Result.success("歌词文件上传成功", result);
    }

    @PostMapping("/music")
    public Result<UploadResult> uploadMusic(@RequestParam("file") MultipartFile file) {
        UploadResult result = uploadService.uploadMusic(file);
        return Result.success("歌曲文件上传成功", result);
    }

    @GetMapping("/music/download")
    public void downloadMusic(@RequestParam("fileUrl") String fileUrl, HttpServletResponse response) {
        uploadService.downloadMusic(fileUrl, response);
    }
}