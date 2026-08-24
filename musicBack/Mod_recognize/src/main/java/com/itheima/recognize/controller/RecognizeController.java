package com.itheima.recognize.controller;

import com.itheima.domain.common.Result;
import com.itheima.recognize.dto.RecognizeResult;
import com.itheima.recognize.service.FingerprintService;
import com.itheima.recognize.service.MatchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * 听歌识曲接口
 */
@RestController
@RequestMapping("/recognize")
public class RecognizeController {

    private final MatchService matchService;
    private final FingerprintService fingerprintService;

    public RecognizeController(MatchService matchService, FingerprintService fingerprintService) {
        this.matchService = matchService;
        this.fingerprintService = fingerprintService;
    }

    @Value("${recognize.upload-base-url}")
    private String uploadBaseUrl;

    /**
     * 识别录音片段
     */
    @PostMapping
    public Result<RecognizeResult> recognize(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "录音文件不能为空");
        }
        try {
            File temp = saveToTemp(file);
            RecognizeResult result = matchService.match(temp);
            temp.delete();
            return result.getSuccess()
                    ? Result.success("识别成功", result)
                    : Result.success("未识别到歌曲", result);
        } catch (Exception e) {
            return Result.error(500, "听歌识曲失败：" + e.getMessage());
        }
    }

    /**
     * 为歌曲注册指纹（歌曲上架/审核通过后可调用）
     */
    @PostMapping("/register")
    public Result<Integer> register(@RequestParam("file") MultipartFile file,
                                    @RequestParam("musicId") Integer musicId) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "音频文件不能为空");
        }
        if (musicId == null) {
            return Result.error(400, "歌曲ID不能为空");
        }
        try {
            File temp = saveToTemp(file);
            int count = fingerprintService.register(temp, musicId);
            temp.delete();
            return Result.success("指纹注册成功，共 " + count + " 条", count);
        } catch (Exception e) {
            return Result.error(500, "指纹注册失败：" + e.getMessage());
        }
    }

    /**
     * 按 music_url 自动注册指纹（供 Mod_admin 审核通过时调用）
     */
    @PostMapping("/registerByUrl")
    public Result<Integer> registerByUrl(@RequestParam("musicId") Integer musicId,
                                         @RequestParam("musicUrl") String musicUrl) {
        if (musicId == null || musicUrl == null || musicUrl.isBlank()) {
            return Result.error(400, "歌曲ID和音频URL不能为空");
        }
        try {
            String fullUrl = musicUrl.startsWith("http") ? musicUrl : uploadBaseUrl + musicUrl;
            File temp = downloadToTemp(fullUrl);
            int count = fingerprintService.register(temp, musicId);
            temp.delete();
            return Result.success("指纹注册成功，共 " + count + " 条", count);
        } catch (Exception e) {
            return Result.error(500, "指纹注册失败：" + e.getMessage());
        }
    }

    private File downloadToTemp(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<java.io.InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new RuntimeException("下载音频失败，HTTP " + response.statusCode());
        }
        File temp = File.createTempFile("recognize-download-", ".tmp");
        try (java.io.InputStream in = response.body()) {
            Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return temp;
    }

    private File saveToTemp(MultipartFile file) throws Exception {
        File temp = File.createTempFile("recognize-", ".tmp");
        Files.copy(file.getInputStream(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return temp;
    }
}