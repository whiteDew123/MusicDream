package com.itheima.upload.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.image.path}")
    private String imagePath;

    @Value("${upload.lrc.path}")
    private String lrcPath;

    @Value("${upload.music.path}")
    private String musicPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/image/**")
                .addResourceLocations("file:" + imagePath);
        registry.addResourceHandler("/uploads/lrc/**")
                .addResourceLocations("file:" + lrcPath);
        registry.addResourceHandler("/uploads/music/**")
                .addResourceLocations("file:" + musicPath);
    }
}