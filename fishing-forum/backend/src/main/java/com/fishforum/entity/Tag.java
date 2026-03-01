package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 标签实体
 */
@Data
@TableName("tags")
public class Tag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long sectionId;
    private String color;
}
