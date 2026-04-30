package com.fishforum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LikeRequest {
    @NotNull(message = "目标不能为空")
    private Long targetId;

    @NotBlank(message = "目标类型不能为空")
    private String targetType;
}
