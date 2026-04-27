package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fishforum.common.JwtUtil;
import com.fishforum.common.Result;
import com.fishforum.entity.User;
import com.fishforum.mapper.*;
import com.fishforum.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
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
    @Transactional
    public Result<?> register(String username, String password, String email) {
        // 输入验证
        if (username == null || username.trim().length() < 2 || username.trim().length() > 20)
            return Result.error(400, "用户名长度必须在2-20个字符");
        if (password == null || password.length() < 6)
            return Result.error(400, "密码至少6位");
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
            return Result.error(400, "邮箱格式不正确");

        // 检查用户名是否已存在
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username.trim())) != null)
            return Result.error(400, "用户名已存在");
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setAvatar("/default-avatar.png");
        user.setRole("USER");
        user.setBio("");
        userMapper.insert(user);
        return Result.ok("注册成功");
    }

    // 用户登录
    public Result<?> login(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty())
            return Result.error(400, "请输入用户名和密码");
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPassword()))
            return Result.error(400, "用户名或密码错误");
        if (Boolean.TRUE.equals(user.getIsBanned()))
            return Result.error(403, "账号已被封禁，请联系管理员");
        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", UserVO.from(user));
        return Result.ok("登录成功", data);
    }

    // 获取当前用户信息
    public Result<?> getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null)
            return Result.error(404, "用户不存在");
        enrichUser(user);
        return Result.ok(UserVO.from(user));
    }

    // 获取用户公开资料
    public Result<?> getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null)
            return Result.error(404, "用户不存在");
        enrichUser(user);
        return Result.ok(UserVO.from(user));
    }

    // 用户成长信息：基于现有行为数据计算，避免新增积分流水前就引入复杂状态
    public Result<?> getGrowthProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null)
            return Result.error(404, "用户不存在");
        long posts = postMapper.selectCount(
                new LambdaQueryWrapper<com.fishforum.entity.Post>().eq(com.fishforum.entity.Post::getUserId, userId));
        long followers = followMapper.selectCount(
                new LambdaQueryWrapper<com.fishforum.entity.Follow>().eq(com.fishforum.entity.Follow::getFollowingId,
                        userId));
        long following = followMapper.selectCount(
                new LambdaQueryWrapper<com.fishforum.entity.Follow>().eq(com.fishforum.entity.Follow::getFollowerId,
                        userId));
        int points = (int) (posts * 10 + followers * 5 + following);
        int level = Math.max(1, points / 100 + 1);
        List<String> badges = new ArrayList<>();
        if (posts >= 1)
            badges.add("首帖纪念");
        if (posts >= 10)
            badges.add("活跃发帖人");
        if (followers >= 5)
            badges.add("受欢迎钓友");
        if (following >= 5)
            badges.add("社交达人");
        Map<String, Object> data = new HashMap<>();
        data.put("points", points);
        data.put("level", level);
        data.put("posts", posts);
        data.put("followers", followers);
        data.put("following", following);
        data.put("badges", badges);
        data.put("nextLevelPoints", level * 100);
        return Result.ok(data);
    }

    // 更新用户信息
    @Transactional
    public Result<?> updateProfile(Long userId, User updateUser) {
        User user = userMapper.selectById(userId);
        if (user == null)
            return Result.error(404, "用户不存在");
        if (updateUser.getBio() != null)
            user.setBio(updateUser.getBio());
        if (updateUser.getEmail() != null)
            user.setEmail(updateUser.getEmail());
        if (updateUser.getAvatar() != null)
            user.setAvatar(updateUser.getAvatar());
        userMapper.updateById(user);
        return Result.ok("更新成功", UserVO.from(user));
    }

    // 修改密码
    @Transactional
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

}
