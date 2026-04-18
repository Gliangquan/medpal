package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.exception.BusinessException;
import com.jcen.medpal.mapper.CommunityCommentMapper;
import com.jcen.medpal.mapper.CommunityLikeMapper;
import com.jcen.medpal.mapper.CommunityPostMapper;
import com.jcen.medpal.model.entity.CommunityComment;
import com.jcen.medpal.model.entity.CommunityLike;
import com.jcen.medpal.model.entity.CommunityPost;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.CommunityService;
import com.jcen.medpal.service.UserService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class CommunityServiceImpl implements CommunityService {

    @Resource
    private CommunityPostMapper communityPostMapper;

    @Resource
    private CommunityCommentMapper communityCommentMapper;

    @Resource
    private CommunityLikeMapper communityLikeMapper;

    @Resource
    private UserService userService;

    @Override
    public CommunityPost createPost(Long userId, CommunityPost post) {
        if (userId == null || post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(post.getTitle()) || StringUtils.isBlank(post.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题和内容不能为空");
        }
        post.setUserId(userId);
        post.setStatus("published");
        post.setCommentCount(0);
        post.setLikeCount(0);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        post.setIsDelete(0);
        communityPostMapper.insert(post);
        return post;
    }

    @Override
    public IPage<CommunityPost> listPosts(long current, long size, String keyword, Long userId) {
        Page<CommunityPost> page = new Page<>(current, size);
        QueryWrapper<CommunityPost> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0)
                .eq("status", "published")
                .eq(userId != null, "user_id", userId)
                .and(StringUtils.isNotBlank(keyword), q -> q.like("title", keyword).or().like("content", keyword))
                .orderByDesc("create_time");
        return communityPostMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getPostDetail(Long postId, Long viewerId) {
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null || post.getIsDelete() != null && post.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("post", enrichPost(post, viewerId));
        result.put("comments", listComments(postId, viewerId));
        return result;
    }

    @Override
    public CommunityComment createComment(Long userId, CommunityComment comment) {
        if (userId == null || comment == null || comment.getPostId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(comment.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        }
        CommunityPost post = communityPostMapper.selectById(comment.getPostId());
        if (post == null || post.getIsDelete() != null && post.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
        }
        comment.setUserId(userId);
        comment.setStatus("published");
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        comment.setIsDelete(0);
        communityCommentMapper.insert(comment);
        communityPostMapper.update(null, new UpdateWrapper<CommunityPost>()
                .eq("id", comment.getPostId())
                .setSql("comment_count = IFNULL(comment_count, 0) + 1"));
        return comment;
    }

    @Override
    public List<Map<String, Object>> listComments(Long postId, Long viewerId) {
        List<CommunityComment> comments = communityCommentMapper.selectList(new QueryWrapper<CommunityComment>()
                .eq("post_id", postId)
                .eq("is_delete", 0)
                .eq("status", "published")
                .orderByAsc("create_time"));
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> userIds = comments.stream().map(CommunityComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, item -> item));
        return comments.stream().map(comment -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", comment.getId());
            item.put("postId", comment.getPostId());
            item.put("userId", comment.getUserId());
            item.put("parentId", comment.getParentId());
            item.put("content", comment.getContent());
            item.put("imageUrls", comment.getImageUrls());
            item.put("likeCount", comment.getLikeCount() == null ? 0 : comment.getLikeCount());
            item.put("createTime", comment.getCreateTime());
            User user = userMap.get(comment.getUserId());
            item.put("authorName", user == null ? "用户" : user.getUserName());
            item.put("authorAvatar", user == null ? null : user.getUserAvatar());
            item.put("liked", hasLiked(viewerId, "comment", comment.getId()));
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> togglePostLike(Long userId, Long postId) {
        return toggleLike(userId, "post", postId, () -> communityPostMapper.update(null,
                new UpdateWrapper<CommunityPost>().eq("id", postId).setSql("like_count = IFNULL(like_count, 0) + 1")),
            () -> communityPostMapper.update(null,
                new UpdateWrapper<CommunityPost>().eq("id", postId).setSql("like_count = GREATEST(IFNULL(like_count, 0) - 1, 0)")),
            () -> fetchPostLikeCount(postId));
    }

    @Override
    public Map<String, Object> toggleCommentLike(Long userId, Long commentId) {
        return toggleLike(userId, "comment", commentId, () -> communityCommentMapper.update(null,
                new UpdateWrapper<CommunityComment>().eq("id", commentId).setSql("like_count = IFNULL(like_count, 0) + 1")),
            () -> communityCommentMapper.update(null,
                new UpdateWrapper<CommunityComment>().eq("id", commentId).setSql("like_count = GREATEST(IFNULL(like_count, 0) - 1, 0)")),
            () -> fetchCommentLikeCount(commentId));
    }

    @Override
    public Map<String, Object> toggleCompanionLike(Long userId, Long companionId) {
        return toggleLike(userId, "companion", companionId, () -> userService.lambdaUpdate()
                .eq(User::getId, companionId)
                .setSql("service_count = IFNULL(service_count, 0) + 1")
                .update(),
            () -> userService.lambdaUpdate()
                .eq(User::getId, companionId)
                .setSql("service_count = GREATEST(IFNULL(service_count, 0) - 1, 0)")
                .update(),
            () -> fetchCompanionLikeCount(companionId));
    }

    @Override
    public List<Map<String, Object>> listGoldCompanions(int limit) {
        int finalLimit = Math.max(1, Math.min(limit, 20));
        List<User> companions = userService.lambdaQuery()
                .eq(User::getUserRole, "companion")
                .eq(User::getStatus, 1)
                .eq(User::getRealNameStatus, "approved")
                .eq(User::getQualificationStatus, "approved")
                .list();
        if (companions.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Long> likeCountMap = communityLikeMapper.selectList(new QueryWrapper<CommunityLike>()
                        .eq("target_type", "companion")
                        .eq("is_delete", 0))
                .stream()
                .collect(Collectors.groupingBy(CommunityLike::getTargetId, Collectors.counting()));
        return companions.stream()
                .sorted(Comparator.comparingLong((User user) -> likeCountMap.getOrDefault(user.getId(), 0L)).reversed()
                        .thenComparing(user -> user.getRating() == null ? 0D : user.getRating(), Comparator.reverseOrder()))
                .limit(finalLimit)
                .map(user -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", user.getId());
                    item.put("userName", user.getUserName());
                    item.put("userAvatar", user.getUserAvatar());
                    item.put("rating", user.getRating());
                    item.put("serviceCount", user.getServiceCount());
                    item.put("specialties", user.getSpecialties());
                    item.put("likeCount", likeCountMap.getOrDefault(user.getId(), 0L));
                    return item;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> toggleLike(Long userId, String targetType, Long targetId, Runnable likeAction,
                                           Runnable unlikeAction, java.util.function.Supplier<Long> countSupplier) {
        if (userId == null || targetId == null || StringUtils.isBlank(targetType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<CommunityLike> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("target_type", targetType).eq("target_id", targetId);
        CommunityLike existing = communityLikeMapper.selectOne(wrapper);
        boolean liked;
        if (existing != null && existing.getIsDelete() != null && existing.getIsDelete() == 0) {
            communityLikeMapper.update(null, new UpdateWrapper<CommunityLike>()
                    .eq("id", existing.getId())
                    .set("is_delete", 1));
            unlikeAction.run();
            liked = false;
        } else if (existing != null) {
            communityLikeMapper.update(null, new UpdateWrapper<CommunityLike>()
                    .eq("id", existing.getId())
                    .set("is_delete", 0)
                    .set("create_time", LocalDateTime.now()));
            likeAction.run();
            liked = true;
        } else {
            CommunityLike like = new CommunityLike();
            like.setUserId(userId);
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            like.setCreateTime(LocalDateTime.now());
            like.setIsDelete(0);
            communityLikeMapper.insert(like);
            likeAction.run();
            liked = true;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", countSupplier.get());
        return result;
    }

    private Map<String, Object> enrichPost(CommunityPost post, Long viewerId) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", post.getId());
        item.put("userId", post.getUserId());
        item.put("title", post.getTitle());
        item.put("content", post.getContent());
        item.put("imageUrls", post.getImageUrls());
        item.put("commentCount", post.getCommentCount() == null ? 0 : post.getCommentCount());
        item.put("likeCount", post.getLikeCount() == null ? 0 : post.getLikeCount());
        item.put("createTime", post.getCreateTime());
        User author = userService.getById(post.getUserId());
        item.put("authorName", author == null ? "用户" : author.getUserName());
        item.put("authorAvatar", author == null ? null : author.getUserAvatar());
        item.put("liked", hasLiked(viewerId, "post", post.getId()));
        return item;
    }

    private boolean hasLiked(Long userId, String targetType, Long targetId) {
        if (userId == null || targetId == null) {
            return false;
        }
        return communityLikeMapper.selectCount(new QueryWrapper<CommunityLike>()
                .eq("user_id", userId)
                .eq("target_type", targetType)
                .eq("target_id", targetId)
                .eq("is_delete", 0)) > 0;
    }

    private Long fetchPostLikeCount(Long postId) {
        CommunityPost post = communityPostMapper.selectById(postId);
        return post == null || post.getLikeCount() == null ? 0L : post.getLikeCount().longValue();
    }

    private Long fetchCommentLikeCount(Long commentId) {
        CommunityComment comment = communityCommentMapper.selectById(commentId);
        return comment == null || comment.getLikeCount() == null ? 0L : comment.getLikeCount().longValue();
    }

    private Long fetchCompanionLikeCount(Long companionId) {
        return communityLikeMapper.selectCount(new QueryWrapper<CommunityLike>()
                .eq("target_type", "companion")
                .eq("target_id", companionId)
                .eq("is_delete", 0));
    }
}
