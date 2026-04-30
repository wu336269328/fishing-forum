package com.fishforum.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SpotCreateRequest {
    @NotBlank(message = "钓点名称不能为空")
    @Size(max = 100, message = "钓点名称不能超过100个字符")
    private String name;

    @Size(max = 2000, message = "钓点描述不能超过2000字")
    private String description;

    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度范围不合法")
    @DecimalMax(value = "90.0", message = "纬度范围不合法")
    private Double latitude;

    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度范围不合法")
    @DecimalMax(value = "180.0", message = "经度范围不合法")
    private Double longitude;

    private String fishTypes;

    @NotBlank(message = "钓点类型不能为空")
    private String spotType;

    private String openTime;
    private String bestSeason;
    private String feeInfo;
    private String noFishingNotice;
    private String imageUrl;
}
