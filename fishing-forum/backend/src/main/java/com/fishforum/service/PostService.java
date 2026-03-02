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
    private final CatchRecordMapper catchRecordMapper;
    private final GearReviewMapper gearReviewMapper;

    // 获取帖子列表（分页，支持类型和标签筛选）
    public Result<?> listPosts(int page, int size, Long sectionId, String keyword, String sort, String postType,
            Long tagId) {
        Page<Post> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();

        // 按板块筛选
        if (sectionId != null)
            wrapper.eq(Post::getSectionId, sectionId);
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
        }
        // 按帖子类型筛选
        if (postType != null && !postType.isEmpty())
            wrapper.eq(Post::getPostType, postType);
        // 按标签筛选
        if (tagId != null)
            wrapper.eq(Post::getTagId, tagId);
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
            return Result.error(404, "帖子不存在");
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
        // 附加渔获/测评元数据
        Map<String, Object> data = new HashMap<>();
        data.put("post", post);
        if ("CATCH".equals(post.getPostType())) {
            CatchRecord cr = catchRecordMapper.selectOne(
                    new LambdaQueryWrapper<CatchRecord>().eq(CatchRecord::getPostId, id));
            data.put("catchRecord", cr);
        } else if ("REVIEW".equals(post.getPostType())) {
            GearReview gr = gearReviewMapper.selectOne(
                    new LambdaQueryWrapper<GearReview>().eq(GearReview::getPostId, id));
            data.put("gearReview", gr);
        }
        return Result.ok(data);
    }

    // 发帖
    public Result<?> createPost(Post post, Long userId) {
        if (post.getTitle() == null || post.getTitle().trim().isEmpty())
            return Result.error(400, "标题不能为空");
        if (post.getTitle().length() > 100)
            return Result.error(400, "标题不能超过100个字符");
        if (post.getContent() == null || post.getContent().isEmpty())
            return Result.error(400, "内容不能为空");
        post.setTitle(post.getTitle().trim());
        post.setUserId(userId);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(false);
        post.setIsFeatured(false);
        if (post.getPostType() == null)
            post.setPostType("NORMAL");
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
            return Result.error(404, "帖子不存在");
        if (!post.getUserId().equals(userId))
            return Result.error(403, "无权修改");
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
            return Result.error(404, "帖子不存在");
        if (!post.getUserId().equals(userId) && !"ADMIN".equals(role))
            return Result.error(403, "无权删除");
        postMapper.deleteById(id);
        // 减少板块帖子计数
        Section section = sectionMapper.selectById(post.getSectionId());
        if (section != null && section.getPostCount() > 0) {
            section.setPostCount(section.getPostCount() - 1);
            sectionMapper.updateById(section);
        }
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
            return Result.error(404, "帖子不存在");
        post.setIsTop(!post.getIsTop());
        postMapper.updateById(post);
        return Result.ok(post.getIsTop() ? "已置顶" : "已取消置顶");
    }

    // 精华/取消精华（管理员）
    public Result<?> toggleFeatured(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null)
            return Result.error(404, "帖子不存在");
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

    // 填充帖子的作者、板块和标签信息
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
        if (post.getTagId() != null) {
            Tag tag = tagMapper.selectById(post.getTagId());
            if (tag != null)
                post.setTagName(tag.getName());
        }
    }

    // 热门帖子 Top N
    public Result<?> getHotPosts(int limit) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getViewCount)
                .last("LIMIT " + Math.min(limit, 20));
        List<Post> posts = postMapper.selectList(wrapper);
        posts.forEach(this::enrichPost);
        return Result.ok(posts);
    }

    // 热门标签（按帖子数量统计）
    public Result<?> getHotTags(int limit) {
        List<Tag> allTags = tagMapper.selectList(null);
        // 统计每个标签的帖子数
        allTags.forEach(tag -> {
            long count = postMapper.selectCount(
                    new LambdaQueryWrapper<Post>().eq(Post::getTagId, tag.getId()));
            tag.setPostCount((int) count);
        });
        allTags.sort((a, b) -> (b.getPostCount() != null ? b.getPostCount() : 0)
                - (a.getPostCount() != null ? a.getPostCount() : 0));
        return Result.ok(allTags.subList(0, Math.min(limit, allTags.size())));
    }

    // 保存渔获记录
    public Result<?> saveCatchRecord(CatchRecord record) {
        catchRecordMapper.insert(record);
        return Result.ok(record);
    }

    // 保存装备测评
    public Result<?> saveGearReview(GearReview review) {
        gearReviewMapper.insert(review);
        return Result.ok(review);
    }
}
