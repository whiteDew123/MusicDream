package com.itheima.upload.config;

import com.itheima.upload.util.AudioDurationUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AudioDurationConfig {

    @Value("${audio.ffprobe-path:ffprobe}")
    private String ffprobePath;

    @PostConstruct
    public void init() {
        AudioDurationUtil.setFfprobePath(ffprobePath);
    }
}