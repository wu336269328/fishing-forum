package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 装备测评实体
 */
@Data
@TableName("gear_reviews")
public class GearReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private String brand; // 品牌
    private String model; // 型号
    private String gearCategory; // 分类: 鱼竿/鱼线/鱼钩/浮漂/饵料/其他
    private Double price; // 价格(元)
    private Integer rating; // 评分1-5
    private String pros; // 优点
    private String cons; // 缺点
    private String photoUrl; // 装备照片
    private LocalDateTime createdAt;
}
