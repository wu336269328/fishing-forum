package com.fishforum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageCreateRequest {
    @NotNull(message = "接收者不能为空")
    private Long receiverId;

    @NotBlank(message = "私信内容不能为空")
    @Size(max = 2000, message = "私信内容不能超过2000字")
    private String content;
}
