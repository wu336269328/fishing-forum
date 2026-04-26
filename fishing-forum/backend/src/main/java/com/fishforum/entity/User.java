package com.fishforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String avatar;
    private String bio;
    private String role; // USER / ADMIN
    private Boolean isBanned;
    private LocalDateTime mutedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    // 非数据库字段 - 粉丝数和关注数
    @TableField(exist = false)
    private Integer followerCount;
    @TableField(exist = false)
    private Integer followingCount;
    @TableField(exist = false)
    private Integer postCount;
}
