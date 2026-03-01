package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 举报实体
 */
@Data
@TableName("reports")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reporterId;
    private Long targetId;
    private String targetType; // POST / COMMENT / USER
    private String reason;
    private String status; // PENDING / RESOLVED / REJECTED
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String reporterName;
}
