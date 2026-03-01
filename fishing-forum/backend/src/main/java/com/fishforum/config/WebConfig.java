package com.fishforum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源配置 - 映射上传文件目录
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:./uploads/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保使用绝对路径
        String absPath = new File(uploadPath).getAbsolutePath() + "/";
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:" + absPath);
    }
}
