package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fishforum.common.Result;
import com.fishforum.entity.*;
import com.fishforum.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialServiceTest {

    @Mock FollowMapper followMapper;
    @Mock MessageMapper messageMapper;
    @Mock NotificationMapper notificationMapper;
    @Mock UserMapper userMapper;
    @Mock SimpMessagingTemplate messagingTemplate;
    @InjectMocks SocialService socialService;

    @Test
    void toggleFollowRejectsSelfAndCreatesNotificationForNewFollow() {
        assertThat(socialService.toggleFollow(3L, 3L).getCode()).isEqualTo(500);
        when(followMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Result<?> result = socialService.toggleFollow(4L, 3L);

        assertThat(result.getData()).isEqualTo("关注成功");
        verify(followMapper).insert(any(Follow.class));
        verify(notificationMapper).insert(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(eq("4"), eq("/queue/notifications"), any(Notification.class));
    }

    @Test
    void toggleFollowDeletesExistingFollow() {
        Follow follow = new Follow();
        follow.setId(8L);
        when(followMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(follow);

        Result<?> result = socialService.toggleFollow(4L, 3L);

        assertThat(result.getData()).isEqualTo("已取消关注");
        verify(followMapper).deleteById(8L);
        verify(notificationMapper, never()).insert(any());
    }

    @Test
    void followListsReturnSanitizedUsers() {
        Follow follow = new Follow();
        follow.setFollowerId(3L);
        follow.setFollowingId(4L);
        User user = user(4L, "friend");
        user.setPassword("secret");
        when(followMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(follow));
        when(userMapper.selectById(4L)).thenReturn(user);

        List<?> users = (List<?>) socialService.getFollowings(3L).getData();

        assertThat(((User) users.get(0)).getPassword()).isNull();
        assertThat(((User) users.get(0)).getUsername()).isEqualTo("friend");
    }

    @Test
    void sendMessagePersistsAndPushesToReceiverQueue() {
        Result<?> result = socialService.sendMessage(4L, "hello", 3L);

        assertThat(result.getMessage()).isEqualTo("发送成功");
        verify(messageMapper).insert(any(Message.class));
        verify(messagingTemplate).convertAndSendToUser(eq("4"), eq("/queue/messages"), any(Message.class));
    }

    @Test
    void getConversationMarksIncomingMessagesRead() {
        Message unread = new Message();
        unread.setId(5L);
        unread.setSenderId(4L);
        unread.setReceiverId(3L);
        unread.setIsRead(false);
        Page<Message> page = new Page<>(1, 20);
        page.setRecords(List.of(unread));
        page.setTotal(1);
        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(messageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(unread));

        Result<?> result = socialService.getConversation(4L, 3L, 1, 20);

        assertThat(((Map<?, ?>) result.getData()).get("total")).isEqualTo(1L);
        assertThat(unread.getIsRead()).isTrue();
        verify(messageMapper).updateById(unread);
    }

    @Test
    void unreadCountCombinesNotificationsAndMessages() {
        when(notificationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);
        when(messageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        Map<?, ?> data = (Map<?, ?>) socialService.getUnreadCount(3L).getData();

        assertThat(data.get("notifications")).isEqualTo(2L);
        assertThat(data.get("messages")).isEqualTo(3L);
        assertThat(data.get("total")).isEqualTo(5L);
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
