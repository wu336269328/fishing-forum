package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 词条编辑历史实体
 */
@Data
@TableName("wiki_histories")
public class WikiHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long entryId;
    private String content;
    private Long userId;
    private Integer version;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String authorName;
}
