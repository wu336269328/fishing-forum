package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 渔获日记实体
 */
@Data
@TableName("catch_records")
public class CatchRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private String fishSpecies; // 鱼种
    private Double weight; // 重量(斤)
    private Double length; // 长度(cm)
    private String bait; // 饵料
    private String spotName; // 钓点名称
    private String weather; // 天气
    private String photoUrl; // 渔获照片
    private LocalDate fishingDate; // 垂钓日期
    private LocalDateTime createdAt;
}
