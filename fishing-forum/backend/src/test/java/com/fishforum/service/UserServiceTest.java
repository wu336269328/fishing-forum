package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fishforum.common.JwtUtil;
import com.fishforum.common.Result;
import com.fishforum.entity.User;
import com.fishforum.mapper.FollowMapper;
import com.fishforum.mapper.PostMapper;
import com.fishforum.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserMapper userMapper;
    @Mock FollowMapper followMapper;
    @Mock PostMapper postMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @InjectMocks UserService userService;

    @Test
    void registerValidatesInputAndCreatesBcryptUser() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");

        Result<?> result = userService.register(" fisher ", "secret123", "fisher@example.com");

        assertThat(result.getCode()).isEqualTo(200);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("fisher");
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
        assertThat(captor.getValue().getRole()).isEqualTo("USER");
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new User());

        Result<?> result = userService.register("fisher", "secret123", "fisher@example.com");

        assertThat(result.getCode()).isEqualTo(400);
        verify(userMapper, never()).insert(any());
    }

    @Test
    void loginReturnsTokenAndSanitizedUser() {
        User user = user(3L, "fisher", "hash", "USER");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("secret123", "hash")).thenReturn(true);
        when(jwtUtil.generateToken(3L, "fisher", "USER")).thenReturn("jwt");

        Result<?> result = userService.login("fisher", "secret123");

        assertThat(result.getCode()).isEqualTo(200);
        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertThat(data.get("token")).isEqualTo("jwt");
        assertThat(((User) data.get("user")).getPassword()).isNull();
    }

    @Test
    void changePasswordRequiresOldPassword() {
        User user = user(3L, "fisher", "hash", "USER");
        when(userMapper.selectById(3L)).thenReturn(user);
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        Result<?> result = userService.changePassword(3L, "bad", "newSecret");

        assertThat(result.getCode()).isEqualTo(500);
        verify(userMapper, never()).updateById(any());
    }

    private static User user(Long id, String username, String password, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }
}
