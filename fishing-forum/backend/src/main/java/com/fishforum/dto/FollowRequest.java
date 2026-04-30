package com.fishforum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FollowRequest {
    @NotNull(message = "关注用户不能为空")
    private Long followingId;
}
