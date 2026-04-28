package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("spot_favorites")
public class SpotFavorite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long spotId;
    private LocalDateTime createdAt;
}
