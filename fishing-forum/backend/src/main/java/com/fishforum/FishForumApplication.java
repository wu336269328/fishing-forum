package com.fishforum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 钓鱼论坛 - 启动类
 */
@SpringBootApplication
@EnableCaching
public class FishForumApplication {
    public static void main(String[] args) {
        SpringApplication.run(FishForumApplication.class, args);
    }
}
