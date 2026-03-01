package com.fishforum.controller;

import com.fishforum.common.Result;
import com.fishforum.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 天气查询控制器
 */
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    // 查询天气（公开）
    @GetMapping
    public Result<?> getWeather(@RequestParam(defaultValue = "北京") String city) {
        return weatherService.getWeather(city);
    }
}
