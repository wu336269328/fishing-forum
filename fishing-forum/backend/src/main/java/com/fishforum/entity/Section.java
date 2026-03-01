package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 板块/分区实体
 */
@Data
@TableName("sections")
public class Section {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer sortOrder;
    private Integer postCount;
    private LocalDateTime createdAt;
}
