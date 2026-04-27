package com.fishforum.config;

import com.fishforum.common.UploadPathResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源配置 - 映射上传文件目录
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:./uploads/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:" + UploadPathResolver.resolve(uploadPath) + "/");
    }
}
