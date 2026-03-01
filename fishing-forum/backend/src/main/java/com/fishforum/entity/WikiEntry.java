package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识百科词条实体
 */
@Data
@TableName("wiki_entries")
public class WikiEntry {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String category; // 鱼种/饵料/装备/技巧/常识
    private Long userId;
    private Integer version;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    // 非数据库字段
    @TableField(exist = false)
    private String authorName;
}
