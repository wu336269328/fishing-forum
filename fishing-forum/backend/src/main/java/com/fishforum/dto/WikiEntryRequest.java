package com.fishforum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WikiEntryRequest {
    @NotBlank(message = "词条标题不能为空")
    @Size(max = 100, message = "词条标题不能超过100个字符")
    private String title;

    @NotBlank(message = "词条内容不能为空")
    private String content;

    @NotBlank(message = "词条分类不能为空")
    private String category;
}
