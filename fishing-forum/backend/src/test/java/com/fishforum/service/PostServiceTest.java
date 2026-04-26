package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fishforum.common.Result;
import com.fishforum.entity.*;
import com.fishforum.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock PostMapper postMapper;
    @Mock UserMapper userMapper;
    @Mock SectionMapper sectionMapper;
    @Mock TagMapper tagMapper;
    @Mock LikeMapper likeMapper;
    @Mock FavoriteMapper favoriteMapper;
    @Mock CatchRecordMapper catchRecordMapper;
    @Mock GearReviewMapper gearReviewMapper;
    @InjectMocks PostService postService;

    @Test
    void listPostsPaginatesAndEnrichesAuthorSectionAndTag() {
        Post post = post(9L, 2L, 4L);
        post.setTagId(6L);
        Page<Post> page = new Page<>(1, 10);
        page.setRecords(List.of(post));
        page.setTotal(1);
        when(postMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(userMapper.selectById(2L)).thenReturn(user(2L, "author"));
        Section section = new Section(); section.setId(4L); section.setName("渔获");
        Tag tag = new Tag(); tag.setId(6L); tag.setName("鲫鱼");
        when(sectionMapper.selectById(4L)).thenReturn(section);
        when(tagMapper.selectById(6L)).thenReturn(tag);

        Result<?> result = postService.listPosts(1, 10, 4L, "鱼", "hot", "CATCH", 6L);

        Map<?, ?> data = (Map<?, ?>) result.getData();
        List<?> records = (List<?>) data.get("records");
        Post enriched = (Post) records.get(0);
        assertThat(enriched.getAuthorName()).isEqualTo("author");
        assertThat(enriched.getSectionName()).isEqualTo("渔获");
        assertThat(enriched.getTagName()).isEqualTo("鲫鱼");
    }

    @Test
    void getPostIncrementsViewsAndAddsInteractionAndCatchMetadata() {
        Post post = post(9L, 2L, 4L);
        post.setPostType("CATCH");
        post.setViewCount(10);
        CatchRecord record = new CatchRecord();
        record.setFishSpecies("鲫鱼");
        when(postMapper.selectById(9L)).thenReturn(post);
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(favoriteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(catchRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);

        Result<?> result = postService.getPost(9L, 3L);

        Map<?, ?> data = (Map<?, ?>) result.getData();
        Post returned = (Post) data.get("post");
        assertThat(returned.getViewCount()).isEqualTo(11);
        assertThat(returned.getLiked()).isTrue();
        assertThat(returned.getFavorited()).isTrue();
        assertThat(((CatchRecord) data.get("catchRecord")).getFishSpecies()).isEqualTo("鲫鱼");
        verify(postMapper).updateById(post);
    }

    @Test
    void createPostInitializesCountersAndUpdatesSectionCount() {
        Post post = new Post();
        post.setTitle(" 新帖 ");
        post.setContent("内容");
        post.setSectionId(4L);
        Section section = new Section();
        section.setId(4L);
        section.setPostCount(2);
        when(sectionMapper.selectById(4L)).thenReturn(section);

        Result<?> result = postService.createPost(post, 8L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(post.getTitle()).isEqualTo("新帖");
        assertThat(post.getUserId()).isEqualTo(8L);
        assertThat(post.getPostType()).isEqualTo("NORMAL");
        assertThat(section.getPostCount()).isEqualTo(3);
        verify(postMapper).insert(post);
        verify(sectionMapper).updateById(section);
    }

    @Test
    void updateAndDeleteRequireOwnerUnlessAdmin() {
        Post existing = post(9L, 2L, 4L);
        when(postMapper.selectById(9L)).thenReturn(existing);

        Result<?> update = postService.updatePost(9L, new Post(), 99L);
        Result<?> delete = postService.deletePost(9L, 99L, "USER");

        assertThat(update.getCode()).isEqualTo(403);
        assertThat(delete.getCode()).isEqualTo(403);
        verify(postMapper, never()).deleteById(anyLong());
    }

    @Test
    void toggleTopAndFeaturedFlipFlags() {
        Post post = post(9L, 2L, 4L);
        post.setIsTop(false);
        post.setIsFeatured(false);
        when(postMapper.selectById(9L)).thenReturn(post);

        assertThat(postService.toggleTop(9L).getData()).isEqualTo("已置顶");
        assertThat(postService.toggleFeatured(9L).getData()).isEqualTo("已设为精华");
        assertThat(post.getIsTop()).isTrue();
        assertThat(post.getIsFeatured()).isTrue();
        verify(postMapper, times(2)).updateById(post);
    }

    @Test
    void saveCatchAndGearRecordsInsertExtensionRows() {
        CatchRecord catchRecord = new CatchRecord();
        GearReview gearReview = new GearReview();

        postService.saveCatchRecord(catchRecord);
        postService.saveGearReview(gearReview);

        verify(catchRecordMapper).insert(catchRecord);
        verify(gearReviewMapper).insert(gearReview);
    }

    private static Post post(Long id, Long userId, Long sectionId) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(userId);
        post.setSectionId(sectionId);
        post.setTitle("title");
        post.setContent("content");
        post.setPostType("NORMAL");
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        return post;
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setAvatar("/avatar.png");
        return user;
    }
}
