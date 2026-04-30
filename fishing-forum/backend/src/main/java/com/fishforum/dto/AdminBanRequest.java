package com.fishforum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminBanRequest {
    @NotNull(message = "封禁状态不能为空")
    private Boolean banned;
}
