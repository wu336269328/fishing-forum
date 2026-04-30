package com.fishforum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportHandleRequest {
    @NotBlank(message = "处理动作不能为空")
    private String action;

    @Size(max = 500, message = "审核备注不能超过500字")
    private String reviewNote;
}
