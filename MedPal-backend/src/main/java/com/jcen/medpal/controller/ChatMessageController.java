package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.entity.ChatMessage;
import com.jcen.medpal.service.ChatMessageService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

/**
 * 聊天消息控制器
 */
@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    @Resource
    private ChatMessageService chatMessageService;

    @PostMapping("/send")
    public Object sendMessage(@RequestBody ChatMessage message) {
        try {
            ChatMessage result = chatMessageService.sendMessage(message);
            return ResultUtils.success(result);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            return ResultUtils.error(40000, (errorMessage == null || errorMessage.trim().isEmpty()) ? "发送消息失败" : errorMessage);
        }
    }

    @GetMapping("/history")
    public Object getChatHistory(@RequestParam Long userId,
                               @RequestParam Long companionId,
                               @RequestParam(defaultValue = "1") long current,
                               @RequestParam(defaultValue = "20") long size) {
        try {
            IPage<ChatMessage> result = chatMessageService.getChatHistoryPage(userId, companionId, current, size);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/search")
    public Object searchMessages(@RequestParam Long userId,
                                @RequestParam String keyword,
                                @RequestParam(defaultValue = "1") long current,
                                @RequestParam(defaultValue = "20") long size) {
        try {
            IPage<ChatMessage> result = chatMessageService.searchMessages(userId, keyword, current, size);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "搜索失败");
        }
    }

    @GetMapping("/unread")
    public Object getUnreadMessages(@RequestParam Long userId) {
        try {
            List<ChatMessage> result = chatMessageService.getUnreadMessages(userId);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @PostMapping("/read/{id}")
    public Object markAsRead(@PathVariable Long id) {
        try {
            boolean result = chatMessageService.markAsRead(id);
            return result ? ResultUtils.success("已读") : ResultUtils.error(40000, "操作失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "操作失败");
        }
    }

    @PostMapping("/conversation/read")
    public Object markConversationAsRead(@RequestParam Long userId,
                                        @RequestParam Long companionId) {
        try {
            boolean result = chatMessageService.markConversationAsRead(userId, companionId);
            return result ? ResultUtils.success("已读") : ResultUtils.error(40000, "操作失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "操作失败");
        }
    }

    @GetMapping("/conversations")
    public Object getConversations(@RequestParam Long userId) {
        try {
            var result = chatMessageService.getConversations(userId);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @DeleteMapping("/{id}")
    public Object deleteMessage(@PathVariable Long id,
                               @RequestParam Long userId) {
        try {
            boolean result = chatMessageService.deleteMessage(id, userId);
            return result ? ResultUtils.success("删除成功") : ResultUtils.error(40000, "删除失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "删除失败");
        }
    }

    @DeleteMapping("/conversation")
    public Object deleteConversation(@RequestParam Long userId,
                                    @RequestParam Long companionId) {
        try {
            boolean result = chatMessageService.deleteConversation(userId, companionId);
            return result ? ResultUtils.success("删除成功") : ResultUtils.error(40000, "删除失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "删除失败");
        }
    }
    
    /**
     * 撤回消息
     * @param messageId 消息ID
     * @param userId 用户ID（从请求参数获取）
     * @param reason 撤回原因
     */
    @PostMapping("/recall/{messageId}")
    public Object recallMessage(@PathVariable Long messageId,
                                @RequestParam Long userId,
                                @RequestParam(required = false, defaultValue = "") String reason) {
        try {
            // 检查是否可以撤回
            if (!chatMessageService.canRecall(messageId, userId)) {
                return ResultUtils.error(40001, "消息已超过2分钟，无法撤回");
            }
            
            boolean result = chatMessageService.recallMessage(messageId, userId, reason);
            return result ? ResultUtils.success("撤回成功") : ResultUtils.error(40000, "撤回失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "撤回失败");
        }
    }
    
    /**
     * 检查消息是否可以撤回
     */
    @GetMapping("/can-recall/{messageId}")
    public Object canRecall(@PathVariable Long messageId,
                           @RequestParam Long userId) {
        try {
            boolean result = chatMessageService.canRecall(messageId, userId);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }
}
