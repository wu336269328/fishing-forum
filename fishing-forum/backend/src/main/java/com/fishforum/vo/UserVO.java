package com.fishforum.vo;

import com.fishforum.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    private String role;
    private Boolean isBanned;
    private LocalDateTime mutedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer followerCount;
    private Integer followingCount;
    private Integer postCount;

    public static UserVO from(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setRole(user.getRole());
        vo.setIsBanned(user.getIsBanned());
        vo.setMutedUntil(user.getMutedUntil());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        vo.setFollowerCount(user.getFollowerCount());
        vo.setFollowingCount(user.getFollowingCount());
        vo.setPostCount(user.getPostCount());
        return vo;
    }
}
