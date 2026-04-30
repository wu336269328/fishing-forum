package com.fishforum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteRequest {
    @NotNull(message = "帖子不能为空")
    private Long postId;
}
