package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fishforum.common.Result;
import com.fishforum.entity.*;
import com.fishforum.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帖子服务 - 帖子CRUD、搜索、板块管理
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final SectionMapper sectionMapper;
    private final TagMapper tagMapper;
    private final LikeMapper likeMapper;
    private final FavoriteMapper favoriteMapper;

    // 获取帖子列表（分页）
    public Result<?> listPosts(int page, int size, Long sectionId, String keyword, String sort) {
        Page<Post> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();

        // 按板块筛选
        if (sectionId != null)
            wrapper.eq(Post::getSectionId, sectionId);
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
        }
        // 排序：置顶帖优先，然后按指定方式排序
        wrapper.orderByDesc(Post::getIsTop);
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(Post::getViewCount);
        } else if ("likes".equals(sort)) {
            wrapper.orderByDesc(Post::getLikeCount);
        } else {
            wrapper.orderByDesc(Post::getCreatedAt); // 默认最新排序
        }

        Page<Post> result = postMapper.selectPage(pageObj, wrapper);
        // 填充作者信息
        result.getRecords().forEach(this::enrichPost);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pages", result.getPages());
        return Result.ok(data);
    }

    // 获取帖子详情
    public Result<?> getPost(Long id, Long currentUserId) {
        Post post = postMapper.selectById(id);
        if (post == null)
            return Result.error("帖子不存在");
        // 增加浏览量
        post.setViewCount(post.getViewCount() + 1);
        postMapper.updateById(post);
        enrichPost(post);
        // 检查当前用户是否点赞/收藏
        if (currentUserId != null) {
            post.setLiked(likeMapper.selectCount(new LambdaQueryWrapper<Like>()
                    .eq(Like::getUserId, currentUserId).eq(Like::getTargetId, id).eq(Like::getTargetType, "POST")) > 0);
            post.setFavorited(favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getUserId, currentUserId).eq(Favorite::getPostId, id)) > 0);
        }
        return Result.ok(post);
    }

    // 发帖
    public Result<?> createPost(Post post, Long userId) {
        post.setUserId(userId);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(false);
        post.setIsFeatured(false);
        postMapper.insert(post);
        // 更新板块帖子计数
        Section section = sectionMapper.selectById(post.getSectionId());
        if (section != null) {
            section.setPostCount(section.getPostCount() + 1);
            sectionMapper.updateById(section);
        }
        return Result.ok("发帖成功", post);
    }

    // 更新帖子
    public Result<?> updatePost(Long id, Post updatePost, Long userId) {
        Post post = postMapper.selectById(id);
        if (post == null)
            return Result.error("帖子不存在");
        if (!post.getUserId().equals(userId))
            return Result.error("无权修改");
        if (updatePost.getTitle() != null)
            post.setTitle(updatePost.getTitle());
        if (updatePost.getContent() != null)
            post.setContent(updatePost.getContent());
        postMapper.updateById(post);
        return Result.ok("更新成功");
    }

    // 删除帖子
    public Result<?> deletePost(Long id, Long userId, String role) {
        Post post = postMapper.selectById(id);
        if (post == null)
            return Result.error("帖子不存在");
        if (!post.getUserId().equals(userId) && !"ADMIN".equals(role)) {
            return Result.error("无权删除");
        }
        postMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    // 获取所有板块
    public Result<?> listSections() {
        List<Section> sections = sectionMapper.selectList(
                new LambdaQueryWrapper<Section>().orderByAsc(Section::getSortOrder));
        return Result.ok(sections);
    }

    // 获取板块下标签
    public Result<?> listTags(Long sectionId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        if (sectionId != null)
            wrapper.eq(Tag::getSectionId, sectionId);
        return Result.ok(tagMapper.selectList(wrapper));
    }

    // 置顶/取消置顶（管理员）
    public Result<?> toggleTop(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null)
            return Result.error("帖子不存在");
        post.setIsTop(!post.getIsTop());
        postMapper.updateById(post);
        return Result.ok(post.getIsTop() ? "已置顶" : "已取消置顶");
    }

    // 精华/取消精华（管理员）
    public Result<?> toggleFeatured(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null)
            return Result.error("帖子不存在");
        post.setIsFeatured(!post.getIsFeatured());
        postMapper.updateById(post);
        return Result.ok(post.getIsFeatured() ? "已设为精华" : "已取消精华");
    }

    // 获取用户的帖子
    public Result<?> getUserPosts(Long userId, int page, int size) {
        Page<Post> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId).orderByDesc(Post::getCreatedAt);
        Page<Post> result = postMapper.selectPage(pageObj, wrapper);
        result.getRecords().forEach(this::enrichPost);
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return Result.ok(data);
    }

    // 填充帖子的作者和板块信息
    private void enrichPost(Post post) {
        User author = userMapper.selectById(post.getUserId());
        if (author != null) {
            post.setAuthorName(author.getUsername());
            post.setAuthorAvatar(author.getAvatar());
        }
        Section section = sectionMapper.selectById(post.getSectionId());
        if (section != null) {
            post.setSectionName(section.getName());
        }
    }
}
