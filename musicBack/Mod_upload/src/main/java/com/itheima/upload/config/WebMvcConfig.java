package com.itheima.upload.config;

import com.itheima.upload.util.ResourcePathResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String imagePath = ResourcePathResolver.resolveDir(ResourcePathResolver.IMAGE);
        String lrcPath = ResourcePathResolver.resolveDir(ResourcePathResolver.LRC);
        String musicPath = ResourcePathResolver.resolveDir(ResourcePathResolver.MUSIC);

        // 统一资源目录：musicBack/resource/（自动定位，不依赖进程工作目录）
        // 兼容两套 URL：上传服务返回的 /uploads/xxx，以及历史数据的 /music /img /lyric
        registry.addResourceHandler("/uploads/image/**", "/img/**")
                .addResourceLocations("file:" + imagePath);
        registry.addResourceHandler("/uploads/lrc/**", "/lyric/**")
                .addResourceLocations("file:" + lrcPath);
        registry.addResourceHandler("/uploads/music/**", "/music/**")
                .addResourceLocations("file:" + musicPath);
    }
}
