package com.fishforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fishforum.common.PageUtils;
import com.fishforum.common.Result;
import com.fishforum.dto.PostCreateRequest;
import com.fishforum.entity.*;
import com.fishforum.mapper.*;
import com.fishforum.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final CommentMapper commentMapper;
    private final ReportMapper reportMapper;
    private final CatchRecordMapper catchRecordMapper;
    private final GearReviewMapper gearReviewMapper;

    private static final Set<String> POST_TYPES = Set.of("NORMAL", "CATCH", "REVIEW");

    // 获取帖子列表（分页，支持类型和标签筛选）
    public Result<?> listPosts(int page, int size, Long sectionId, String keyword, String sort, String postType,
            Long tagId) {
        Page<Post> pageObj = PageUtils.pageRequest(page, size);
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
        List<PostVO> records = enrichPosts(result.getRecords());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        data.put("pages", result.getPages());
        return Result.ok(data);
    }

    // 获取帖子详情
    public Result<?> getPost(Long id, Long currentUserId) {
        Post post = postMapper.selectById(id);
        if (post == null)
            return Result.error(404, "帖子不存在");
        postMapper.incrementViewCount(id);
        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        PostVO postVO = enrichPosts(List.of(post)).get(0);
        // 检查当前用户是否点赞/收藏
        if (currentUserId != null) {
            postVO.setLiked(likeMapper.selectCount(new LambdaQueryWrapper<Like>()
                    .eq(Like::getUserId, currentUserId).eq(Like::getTargetId, id).eq(Like::getTargetType, "POST")) > 0);
            postVO.setFavorited(favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getUserId, currentUserId).eq(Favorite::getPostId, id)) > 0);
        }
        // 附加渔获/测评元数据
        Map<String, Object> data = new HashMap<>();
        data.put("post", postVO);
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
    @Transactional
    public Result<?> createPost(Post post, Long userId) {
        Result<?> validation = validatePost(post, userId, false);
        if (validation.getCode() != 200) {
            return validation;
        }
        initializePost(post, userId);
        postMapper.insert(post);
        sectionMapper.incrementPostCount(post.getSectionId(), 1);
        return Result.ok("发帖成功", post);
    }

    @Transactional
    public Result<?> createPostWithExtensions(PostCreateRequest request, Long userId) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSectionId(request.getSectionId());
        post.setTagId(request.getTagId());
        post.setPostType(request.getPostType() == null || request.getPostType().isBlank() ? "NORMAL" : request.getPostType());

        Result<?> validation = validatePost(post, userId, true);
        if (validation.getCode() != 200) {
            return validation;
        }
        if ("CATCH".equals(post.getPostType()) && request.getCatchRecord() == null) {
            return Result.error(400, "渔获记录不能为空");
        }
        if ("REVIEW".equals(post.getPostType()) && request.getGearReview() == null) {
            return Result.error(400, "装备测评不能为空");
        }

        initializePost(post, userId);
        postMapper.insert(post);
        if ("CATCH".equals(post.getPostType())) {
            catchRecordMapper.insert(toCatchRecord(request.getCatchRecord(), post.getId()));
        } else if ("REVIEW".equals(post.getPostType())) {
            gearReviewMapper.insert(toGearReview(request.getGearReview(), post.getId()));
        }
        sectionMapper.incrementPostCount(post.getSectionId(), 1);
        return Result.ok("发帖成功", post);
    }

    private Result<?> validatePost(Post post, Long userId, boolean requireReferences) {
        User currentUser = userMapper.selectById(userId);
        if (currentUser != null && Boolean.TRUE.equals(currentUser.getIsBanned())) {
            return Result.error(403, "账号已被封禁");
        }
        if (currentUser != null && currentUser.getMutedUntil() != null && currentUser.getMutedUntil().isAfter(java.time.LocalDateTime.now())) {
            return Result.error(403, "账号已被禁言，暂不能发帖");
        }
        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            return Result.error(400, "标题不能为空");
        }
        if (post.getTitle().length() > 100) {
            return Result.error(400, "标题不能超过100个字符");
        }
        if (post.getContent() == null || post.getContent().isEmpty()) {
            return Result.error(400, "内容不能为空");
        }
        if (post.getPostType() == null || post.getPostType().isBlank()) {
            post.setPostType("NORMAL");
        }
        if (!POST_TYPES.contains(post.getPostType())) {
            return Result.error(400, "帖子类型不合法");
        }
        if (requireReferences) {
            Section section = post.getSectionId() == null ? null : sectionMapper.selectById(post.getSectionId());
            if (section == null) {
                return Result.error(400, "板块不存在");
            }
            if (post.getTagId() != null) {
                Tag tag = tagMapper.selectById(post.getTagId());
                if (tag == null || !post.getSectionId().equals(tag.getSectionId())) {
                    return Result.error(400, "标签不存在或不属于该板块");
                }
            }
        }
        return Result.ok();
    }

    private void initializePost(Post post, Long userId) {
        post.setTitle(post.getTitle().trim());
        post.setUserId(userId);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(false);
        post.setIsFeatured(false);
    }

    private CatchRecord toCatchRecord(PostCreateRequest.CatchRecordRequest request, Long postId) {
        CatchRecord record = new CatchRecord();
        record.setPostId(postId);
        record.setFishSpecies(request.getFishSpecies());
        record.setWeight(request.getWeight());
        record.setLength(request.getLength());
        record.setBait(request.getBait());
        record.setSpotName(request.getSpotName());
        record.setWeather(request.getWeather());
        record.setPhotoUrl(request.getPhotoUrl());
        record.setFishingDate(request.getFishingDate());
        return record;
    }

    private GearReview toGearReview(PostCreateRequest.GearReviewRequest request, Long postId) {
        GearReview review = new GearReview();
        review.setPostId(postId);
        review.setBrand(request.getBrand());
        review.setModel(request.getModel());
        review.setGearCategory(request.getGearCategory());
        review.setPrice(request.getPrice());
        review.setRating(request.getRating());
        review.setPros(request.getPros());
        review.setCons(request.getCons());
        review.setPhotoUrl(request.getPhotoUrl());
        return review;
    }

    // 更新帖子
    @Transactional
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
    @Transactional
    public Result<?> deletePost(Long id, Long userId, String role) {
        Post post = postMapper.selectById(id);
        if (post == null)
            return Result.error(404, "帖子不存在");
        if (!post.getUserId().equals(userId) && !"ADMIN".equals(role))
            return Result.error(403, "无权删除");
        likeMapper.deleteByTarget("POST", id);
        favoriteMapper.deleteByPostId(id);
        List<Long> commentIds = commentMapper.selectList(new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, id))
                .stream()
                .map(Comment::getId)
                .toList();
        for (Long commentId : commentIds) {
            likeMapper.deleteByTarget("COMMENT", commentId);
            reportMapper.deleteByTarget("COMMENT", commentId);
        }
        commentMapper.deleteByPostId(id);
        reportMapper.deleteByTarget("POST", id);
        catchRecordMapper.deleteByPostId(id);
        gearReviewMapper.deleteByPostId(id);
        postMapper.deleteById(id);
        sectionMapper.incrementPostCount(post.getSectionId(), -1);
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
        Page<Post> pageObj = PageUtils.pageRequest(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId).orderByDesc(Post::getCreatedAt);
        Page<Post> result = postMapper.selectPage(pageObj, wrapper);
        List<PostVO> records = enrichPosts(result.getRecords());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        return Result.ok(data);
    }

    // 批量填充帖子的作者、板块和标签信息，避免列表 N+1 查询
    private List<PostVO> enrichPosts(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }
        Map<Long, User> users = mapById(batchUsers(ids(posts, Post::getUserId)), User::getId);
        Map<Long, Section> sections = mapById(batchSections(ids(posts, Post::getSectionId)), Section::getId);
        Map<Long, Tag> tags = mapById(batchTags(ids(posts, Post::getTagId)), Tag::getId);
        return posts.stream().map(post -> {
            User author = users.get(post.getUserId());
            if (author != null) {
                post.setAuthorName(author.getUsername());
                post.setAuthorAvatar(author.getAvatar());
            }
            Section section = sections.get(post.getSectionId());
            if (section != null) {
                post.setSectionName(section.getName());
            }
            if (post.getTagId() != null) {
                Tag tag = tags.get(post.getTagId());
                if (tag != null) {
                    post.setTagName(tag.getName());
                }
            }
            return PostVO.from(post);
        }).collect(Collectors.toList());
    }

    private static <T> Collection<Long> ids(List<Post> posts, Function<Post, Long> extractor) {
        return posts.stream().map(extractor).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static <T> Map<Long, T> mapById(Collection<T> rows, Function<T, Long> idExtractor) {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        return rows.stream().collect(Collectors.toMap(idExtractor, Function.identity()));
    }

    private Collection<User> batchUsers(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : userMapper.selectBatchIds(ids);
    }

    private Collection<Section> batchSections(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : sectionMapper.selectBatchIds(ids);
    }

    private Collection<Tag> batchTags(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : tagMapper.selectBatchIds(ids);
    }

    // 热门帖子 Top N
    public Result<?> getHotPosts(int limit) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getViewCount)
                .last("LIMIT " + Math.min(Math.max(limit, 1), 20));
        List<Post> posts = postMapper.selectList(wrapper);
        return Result.ok(enrichPosts(posts));
    }

    // 热门标签（按帖子数量统计）
    public Result<?> getHotTags(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        List<Tag> allTags = tagMapper.selectList(null);
        // 统计每个标签的帖子数
        allTags.forEach(tag -> {
            long count = postMapper.selectCount(
                    new LambdaQueryWrapper<Post>().eq(Post::getTagId, tag.getId()));
            tag.setPostCount((int) count);
        });
        allTags.sort((a, b) -> (b.getPostCount() != null ? b.getPostCount() : 0)
                - (a.getPostCount() != null ? a.getPostCount() : 0));
        return Result.ok(allTags.subList(0, Math.min(safeLimit, allTags.size())));
    }

    // 保存渔获记录
    @Transactional
    public Result<?> saveCatchRecord(CatchRecord record) {
        catchRecordMapper.insert(record);
        return Result.ok(record);
    }

    // 保存装备测评
    @Transactional
    public Result<?> saveGearReview(GearReview review) {
        gearReviewMapper.insert(review);
        return Result.ok(review);
    }
}
