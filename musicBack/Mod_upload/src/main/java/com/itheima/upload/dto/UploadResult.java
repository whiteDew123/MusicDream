package com.itheima.upload.dto;

import lombok.Data;

@Data
public class UploadResult {
    private String fileName;
    private String filePath;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private Integer duration;
}