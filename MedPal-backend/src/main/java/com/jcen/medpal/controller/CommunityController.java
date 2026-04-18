package com.jcen.medpal.controller;

import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.CommunityComment;
import com.jcen.medpal.model.entity.CommunityPost;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.CommunityService;
import com.jcen.medpal.service.UserService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/community")
public class CommunityController {

    @Resource
    private CommunityService communityService;

    @Resource
    private UserService userService;

    @PostMapping("/post")
    public Object createPost(@RequestBody CommunityPost post, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            return ResultUtils.success(communityService.createPost(loginUser.getId(), post));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(40000, e.getMessage() == null ? "发布失败" : e.getMessage());
        }
    }

    @GetMapping("/posts")
    public Object listPosts(@RequestParam(defaultValue = "1") long current,
                            @RequestParam(defaultValue = "10") long size,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) Long userId) {
        try {
            return ResultUtils.success(communityService.listPosts(current, size, keyword, userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    @GetMapping("/post/{id}")
    public Object getPostDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUserPermitNull(request);
            Long viewerId = loginUser == null ? null : loginUser.getId();
            return ResultUtils.success(communityService.getPostDetail(id, viewerId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    @PostMapping("/comment")
    public Object createComment(@RequestBody CommunityComment comment, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            return ResultUtils.success(communityService.createComment(loginUser.getId(), comment));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(40000, e.getMessage() == null ? "评论失败" : e.getMessage());
        }
    }

    @PostMapping("/post/{id}/like")
    public Object togglePostLike(@PathVariable Long id, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            return ResultUtils.success(communityService.togglePostLike(loginUser.getId(), id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(40000, e.getMessage() == null ? "操作失败" : e.getMessage());
        }
    }

    @PostMapping("/comment/{id}/like")
    public Object toggleCommentLike(@PathVariable Long id, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            return ResultUtils.success(communityService.toggleCommentLike(loginUser.getId(), id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(40000, e.getMessage() == null ? "操作失败" : e.getMessage());
        }
    }

    @PostMapping("/companion/{id}/like")
    public Object toggleCompanionLike(@PathVariable Long id, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            return ResultUtils.success(communityService.toggleCompanionLike(loginUser.getId(), id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(40000, e.getMessage() == null ? "操作失败" : e.getMessage());
        }
    }

    @GetMapping("/gold-companions")
    public Object listGoldCompanions(@RequestParam(defaultValue = "10") int limit) {
        try {
            return ResultUtils.success(communityService.listGoldCompanions(limit));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(40000, e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }
}
