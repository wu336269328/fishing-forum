package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 钓点实体
 */
@Data
@TableName("fishing_spots")
public class FishingSpot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String fishTypes; // 鱼种列表（逗号分隔）
    private String spotType; // 水库/河流/湖泊/海钓/黑坑
    private String openTime;
    private String bestSeason;
    private String feeInfo;
    private String noFishingNotice;
    private Double rating;
    private Integer reviewCount;
    private Long userId;
    private String imageUrl; // 钓点图片
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;

    // 非数据库字段
    @TableField(exist = false)
    private String authorName;
}
