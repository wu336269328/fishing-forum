package com.fishforum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminMuteRequest {
    @NotNull(message = "禁言状态不能为空")
    private Boolean muted;

    private Integer minutes = 60;
}
