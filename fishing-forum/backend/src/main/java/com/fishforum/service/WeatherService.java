package com.fishforum.service;

import com.fishforum.common.Result;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * 天气查询服务 - 调用 uapis.cn 真实天气API
 */
@Service
public class WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_BASE = "https://uapis.cn/api/v1/misc/weather";

    // 查询真实天气
    public Result<?> getWeather(String city) {
        if (city == null || city.isEmpty())
            city = "北京";
        try {
            // 使用UriComponentsBuilder正确编码中文参数
            String url = UriComponentsBuilder.fromHttpUrl(API_BASE)
                    .queryParam("city", city)
                    .queryParam("extended", "true")
                    .queryParam("forecast", "true")
                    .queryParam("indices", "true")
                    .queryParam("lang", "zh")
                    .build()
                    .toUriString();
            @SuppressWarnings("unchecked")
            Map<String, Object> data = restTemplate.getForObject(url, Map.class);
            if (data == null) {
                return Result.error("天气查询失败");
            }
            return Result.ok(data);
        } catch (Exception e) {
            return Result.error("天气查询失败: " + e.getMessage());
        }
    }
}
