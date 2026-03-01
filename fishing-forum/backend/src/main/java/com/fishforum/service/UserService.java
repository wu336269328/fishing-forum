package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fishforum.common.JwtUtil;
import com.fishforum.common.Result;
import com.fishforum.entity.User;
import com.fishforum.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务 - 注册、登录、个人信息管理
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final FollowMapper followMapper;
    private final PostMapper postMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 用户注册
    public Result<?> register(String username, String password, String email) {
        // 检查用户名是否已存在
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0) {
            return Result.error("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 密码加密
        user.setEmail(email);
        user.setAvatar("/default-avatar.png");
        user.setRole("USER");
        user.setBio("");
        userMapper.insert(user);
        return Result.ok("注册成功");
    }

    // 用户登录
    public Result<?> login(String username, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", sanitizeUser(user));
        return Result.ok("登录成功", data);
    }

    // 获取当前用户信息
    public Result<?> getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null)
            return Result.error("用户不存在");
        enrichUser(user);
        return Result.ok(sanitizeUser(user));
    }

    // 获取用户公开资料
    public Result<?> getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null)
            return Result.error("用户不存在");
        enrichUser(user);
        return Result.ok(sanitizeUser(user));
    }

    // 更新用户信息
    public Result<?> updateProfile(Long userId, User updateUser) {
        User user = userMapper.selectById(userId);
        if (user == null)
            return Result.error("用户不存在");
        if (updateUser.getBio() != null)
            user.setBio(updateUser.getBio());
        if (updateUser.getEmail() != null)
            user.setEmail(updateUser.getEmail());
        if (updateUser.getAvatar() != null)
            user.setAvatar(updateUser.getAvatar());
        userMapper.updateById(user);
        return Result.ok("更新成功", sanitizeUser(user));
    }

    // 修改密码
    public Result<?> changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return Result.ok("密码修改成功");
    }

    // 填充用户统计信息
    private void enrichUser(User user) {
        user.setFollowerCount(followMapper.selectCount(
                new LambdaQueryWrapper<com.fishforum.entity.Follow>().eq(com.fishforum.entity.Follow::getFollowingId,
                        user.getId()))
                .intValue());
        user.setFollowingCount(followMapper.selectCount(
                new LambdaQueryWrapper<com.fishforum.entity.Follow>().eq(com.fishforum.entity.Follow::getFollowerId,
                        user.getId()))
                .intValue());
        user.setPostCount(postMapper.selectCount(
                new LambdaQueryWrapper<com.fishforum.entity.Post>().eq(com.fishforum.entity.Post::getUserId,
                        user.getId()))
                .intValue());
    }

    // 隐藏敏感信息（密码）
    private User sanitizeUser(User user) {
        user.setPassword(null);
        return user;
    }
}
