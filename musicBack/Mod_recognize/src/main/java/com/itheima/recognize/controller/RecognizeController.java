package com.itheima.recognize.controller;

import com.itheima.domain.common.Result;
import com.itheima.recognize.dto.RecognizeResult;
import com.itheima.recognize.service.FingerprintService;
import com.itheima.recognize.service.MatchService;
import com.itheima.domain.entity.Music;
import com.itheima.recognize.mapper.MusicMapper;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final MusicMapper musicMapper;

    public RecognizeController(MatchService matchService,
                               FingerprintService fingerprintService,
                               MusicMapper musicMapper) {
        this.matchService = matchService;
        this.fingerprintService = fingerprintService;
        this.musicMapper = musicMapper;
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
            if (count <= 0) {
                return Result.error(500, "未提取到任何指纹，请检查音频文件");
            }
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
            if (count <= 0) {
                return Result.error(500, "未提取到任何指纹，请检查音频文件");
            }
            return Result.success("指纹注册成功，共 " + count + " 条", count);
        } catch (Exception e) {
            return Result.error(500, "指纹注册失败：" + e.getMessage());
        }
    }

    /**
     * 为数据库现有歌曲批量注册指纹（一次性补数据接口）
     */
    @PostMapping("/registerAll")
    public Result<Map<String, Object>> registerAll() {
        List<Music> musics = musicMapper.selectList(null);
        List<Integer> successIds = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        for (Music music : musics) {
            if (music.getMusicUrl() == null || music.getMusicUrl().isBlank()) {
                continue;
            }
            try {
                String fullUrl = music.getMusicUrl().startsWith("http")
                        ? music.getMusicUrl()
                        : uploadBaseUrl + music.getMusicUrl();
                File temp = downloadToTemp(fullUrl);
                int count = fingerprintService.register(temp, music.getMusicId());
                temp.delete();
                if (count > 0) {
                    successIds.add(music.getMusicId());
                } else {
                    failures.add("musicId=" + music.getMusicId() + ": 未提取到指纹");
                }
            } catch (Exception e) {
                failures.add("musicId=" + music.getMusicId() + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("successIds", successIds);
        result.put("failures", failures);
        result.put("successCount", successIds.size());
        result.put("failCount", failures.size());
        return Result.success("批量注册完成", result);
    }

    /**
     * 分析指定歌曲的指纹提取情况（调试用，不入库）
     */
    @PostMapping("/analyze")
    public Result<Map<String, Object>> analyze(@RequestParam("musicId") Integer musicId) {
        Music music = musicMapper.selectById(musicId);
        if (music == null) {
            return Result.error(404, "歌曲不存在");
        }
        if (music.getMusicUrl() == null || music.getMusicUrl().isBlank()) {
            return Result.error(400, "歌曲没有音频URL");
        }
        try {
            String fullUrl = music.getMusicUrl().startsWith("http")
                    ? music.getMusicUrl()
                    : uploadBaseUrl + music.getMusicUrl();
            File temp = downloadToTemp(fullUrl);
            Map<String, Object> data = fingerprintService.analyze(temp);
            temp.delete();
            return Result.success("分析完成", data);
        } catch (Exception e) {
            return Result.error(500, "分析失败：" + e.getMessage());
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
        String ext = extractExtension(url);
        File temp = File.createTempFile("recognize-download-", ext);
        try (java.io.InputStream in = response.body()) {
            Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return temp;
    }

    private String extractExtension(String url) {
        String path = URI.create(url).getPath();
        int dot = path.lastIndexOf('.');
        return dot >= 0 ? path.substring(dot) : ".tmp";
    }

    private File saveToTemp(MultipartFile file) throws Exception {
        String origName = file.getOriginalFilename();
        String ext = ".tmp";
        if (origName != null && origName.contains(".")) {
            ext = origName.substring(origName.lastIndexOf('.'));
        }
        File temp = File.createTempFile("recognize-", ext);
        Files.copy(file.getInputStream(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return temp;
    }
}