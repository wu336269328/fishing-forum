package com.fishforum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportCreateRequest {
    @NotNull(message = "目标不能为空")
    private Long targetId;

    @NotBlank(message = "举报类型不能为空")
    private String targetType;

    @NotBlank(message = "举报原因不能为空")
    @Size(max = 500, message = "举报原因不能超过500字")
    private String reason;
}
