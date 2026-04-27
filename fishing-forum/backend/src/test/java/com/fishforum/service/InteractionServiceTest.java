package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fishforum.common.Result;
import com.fishforum.entity.*;
import com.fishforum.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock CommentMapper commentMapper;
    @Mock LikeMapper likeMapper;
    @Mock FavoriteMapper favoriteMapper;
    @Mock PostMapper postMapper;
    @Mock UserMapper userMapper;
    @Mock ReportMapper reportMapper;
    @Mock SocialService socialService;
    @InjectMocks InteractionService interactionService;

    @Test
    void getCommentsBuildsTreeAndEnrichesAuthors() {
        Comment parent = comment(1L, null, 2L);
        Comment child = comment(2L, 1L, 3L);
        when(commentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(parent, child));
        when(userMapper.selectById(2L)).thenReturn(user(2L, "父用户"));
        when(userMapper.selectById(3L)).thenReturn(user(3L, "子用户"));

        Result<?> result = interactionService.getComments(7L);

        List<?> roots = (List<?>) result.getData();
        Comment root = (Comment) roots.get(0);
        assertThat(root.getAuthorName()).isEqualTo("父用户");
        assertThat(root.getChildren()).hasSize(1);
        assertThat(root.getChildren().get(0).getAuthorName()).isEqualTo("子用户");
    }

    @Test
    void addCommentInsertsAndIncrementsPostCommentCount() {
        Post post = new Post();
        post.setId(7L);
        post.setUserId(9L);
        post.setCommentCount(2);
        when(postMapper.selectById(7L)).thenReturn(post);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user(4L, "被提到"));

        Result<?> result = interactionService.addComment(7L, " 赞 @被提到 ", null, 3L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(post.getCommentCount()).isEqualTo(3);
        verify(commentMapper).insert(any(Comment.class));
        verify(postMapper).updateById(post);
        verify(socialService).sendNotification(eq(9L), eq("COMMENT"), anyString(), anyString(), eq(7L));
        verify(socialService).sendNotification(eq(4L), eq("MENTION"), anyString(), anyString(), eq(7L));
    }

    @Test
    void replyCommentNotifiesParentCommentAuthor() {
        Post post = new Post();
        post.setId(7L);
        post.setUserId(9L);
        post.setCommentCount(2);
        Comment parent = comment(6L, null, 8L);
        when(postMapper.selectById(7L)).thenReturn(post);
        when(commentMapper.selectById(6L)).thenReturn(parent);

        Result<?> result = interactionService.addComment(7L, "回复", 6L, 3L);

        assertThat(result.getCode()).isEqualTo(200);
        verify(socialService).sendNotification(eq(8L), eq("COMMENT_REPLY"), anyString(), anyString(), eq(7L));
    }

    @Test
    void deleteCommentDeletesDirectChildrenAndDecrementsCount() {
        Comment parent = comment(1L, null, 2L);
        parent.setPostId(7L);
        Comment child = comment(2L, 1L, 3L);
        Post post = new Post();
        post.setId(7L);
        post.setCommentCount(4);
        when(commentMapper.selectById(1L)).thenReturn(parent);
        when(commentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(child));
        when(postMapper.selectById(7L)).thenReturn(post);

        Result<?> result = interactionService.deleteComment(1L, 2L, "USER");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(post.getCommentCount()).isEqualTo(2);
        verify(commentMapper).deleteById(2L);
        verify(commentMapper).deleteById(1L);
    }

    @Test
    void toggleLikeCreatesAndRemovesLikeWhileUpdatingPostCount() {
        Post post = new Post();
        post.setId(7L);
        post.setUserId(9L);
        post.setLikeCount(1);
        when(likeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(postMapper.selectById(7L)).thenReturn(post);

        Result<?> result = interactionService.toggleLike(7L, "POST", 3L);

        assertThat(result.getData()).isEqualTo("点赞成功");
        assertThat(post.getLikeCount()).isEqualTo(2);
        verify(likeMapper).insert(any(Like.class));
        verify(socialService).sendNotification(eq(9L), eq("LIKE"), anyString(), anyString(), eq(7L));
    }

    @Test
    void favoriteAndReportCreateRows() {
        when(favoriteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Result<?> fav = interactionService.toggleFavorite(7L, 3L);
        Result<?> report = interactionService.report(7L, "POST", "违规", 3L);

        assertThat(fav.getData()).isEqualTo("收藏成功");
        assertThat(report.getCode()).isEqualTo(200);
        verify(favoriteMapper).insert(any(Favorite.class));
        verify(reportMapper).insert(any(Report.class));
    }

    private static Comment comment(Long id, Long parentId, Long userId) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setPostId(7L);
        comment.setParentId(parentId);
        comment.setUserId(userId);
        comment.setContent("content");
        comment.setLikeCount(0);
        return comment;
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setAvatar("/avatar.png");
        return user;
    }
}
