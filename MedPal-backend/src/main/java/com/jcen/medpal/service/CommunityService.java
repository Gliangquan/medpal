package com.jcen.medpal.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcen.medpal.model.entity.CommunityComment;
import com.jcen.medpal.model.entity.CommunityPost;
import java.util.List;
import java.util.Map;

public interface CommunityService {

    CommunityPost createPost(Long userId, CommunityPost post);

    IPage<CommunityPost> listPosts(long current, long size, String keyword, Long userId);

    Map<String, Object> getPostDetail(Long postId, Long viewerId);

    CommunityComment createComment(Long userId, CommunityComment comment);

    List<Map<String, Object>> listComments(Long postId, Long viewerId);

    Map<String, Object> togglePostLike(Long userId, Long postId);

    Map<String, Object> toggleCommentLike(Long userId, Long commentId);

    Map<String, Object> toggleCompanionLike(Long userId, Long companionId);

    List<Map<String, Object>> listGoldCompanions(int limit);
}
