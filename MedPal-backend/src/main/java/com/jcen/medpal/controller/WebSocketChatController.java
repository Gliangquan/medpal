package com.jcen.medpal.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jcen.medpal.model.entity.ChatMessage;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.ChatMessageService;
import com.jcen.medpal.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 消息控制器
 * 处理实时聊天消息的发送和接收
 */
@RestController
public class WebSocketChatController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketChatController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserService userService;

    /**
     * 处理聊天消息
     * 客户端发送消息到 /app/chat.send/{receiverId}
     * 消息会保存到数据库，并推送给接收方
     */
    @MessageMapping("/chat.send/{receiverId}")
    public void sendChatMessage(Principal principal, 
                                @DestinationVariable Long receiverId, 
                                ChatMessage message) {
        try {
            // 获取发送者ID
            Long senderId = Long.parseLong(principal.getName());
            
            // 构建完整消息
            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            
            // 保存消息到数据库（并创建消息提醒通知）
            chatMessageService.sendMessage(message);
            
            // 构建响应消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "chat");
            response.put("message", message);
            
            // 获取发送者信息
            Map<String, Object> senderInfo = new HashMap<>();
            senderInfo.put("id", senderId);
            try {
                User sender = userService.getById(senderId);
                if (sender != null) {
                    senderInfo.put("nickname", sender.getUserName() != null ? sender.getUserName() : "用户" + senderId);
                    senderInfo.put("avatar", sender.getUserAvatar());
                }
            } catch (Exception e) {
                logger.warn("获取用户信息失败: {}", e.getMessage());
            }
            response.put("sender", senderInfo);
            
            // 推送给接收方（点对点消息）
            messagingTemplate.convertAndSendToUser(
                receiverId.toString(), 
                "/queue/messages", 
                response
            );
            
            // 推送确认给发送方
            messagingTemplate.convertAndSendToUser(
                senderId.toString(), 
                "/queue/messages", 
                response
            );
            
            logger.info("消息发送成功: senderId={}, receiverId={}", senderId, receiverId);
            
        } catch (Exception e) {
            logger.error("消息发送失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理消息已读确认
     * 客户端发送确认到 /app/chat.read/{messageId}
     */
    @MessageMapping("/chat.read/{messageId}")
    public void markMessageAsRead(@DestinationVariable Long messageId, 
                                   Principal principal) {
        try {
            Long userId = Long.parseLong(principal.getName());
            
            // 更新消息状态
            chatMessageService.markAsRead(messageId);
            
            // 构建已读确认消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "read");
            response.put("messageId", messageId);
            response.put("readTime", LocalDateTime.now().toString());
            
            // 通知对方消息已读
            ChatMessage message = chatMessageService.getById(messageId);
            if (message != null && !message.getSenderId().equals(userId)) {
                messagingTemplate.convertAndSendToUser(
                    message.getSenderId().toString(), 
                    "/queue/messages", 
                    response
                );
            }
            
        } catch (Exception e) {
            logger.error("标记已读失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理输入状态通知
     * 通知对方自己正在输入
     */
    @MessageMapping("/chat.typing/{receiverId}")
    public void sendTypingStatus(@DestinationVariable Long receiverId, 
                                  Principal principal) {
        try {
            Long senderId = Long.parseLong(principal.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("type", "typing");
            response.put("senderId", senderId);
            response.put("isTyping", true);
            
            messagingTemplate.convertAndSendToUser(
                receiverId.toString(), 
                "/queue/messages", 
                response
            );
            
        } catch (Exception e) {
            logger.error("输入状态通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理停止输入通知
     */
    @MessageMapping("/chat.stopTyping/{receiverId}")
    public void sendStopTypingStatus(@DestinationVariable Long receiverId, 
                                      Principal principal) {
        try {
            Long senderId = Long.parseLong(principal.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("type", "stopTyping");
            response.put("senderId", senderId);
            response.put("isTyping", false);
            
            messagingTemplate.convertAndSendToUser(
                receiverId.toString(), 
                "/queue/messages", 
                response
            );
            
        } catch (Exception e) {
            logger.error("停止输入通知失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理撤回消息通知
     */
    @MessageMapping("/chat.recall/{messageId}")
    public void recallMessage(@DestinationVariable Long messageId,
                             Principal principal) {
        try {
            Long userId = Long.parseLong(principal.getName());
            
            ChatMessage message = chatMessageService.getById(messageId);
            if (message == null || !message.getSenderId().equals(userId)) {
                return;
            }
            
            // 执行撤回
            chatMessageService.recallMessage(messageId, userId, "");
            
            // 构建撤回通知
            Map<String, Object> response = new HashMap<>();
            response.put("type", "recall");
            response.put("messageId", messageId);
            response.put("senderId", userId);
            response.put("content", "【消息已撤回】");
            
            // 通知对方
            messagingTemplate.convertAndSendToUser(
                message.getReceiverId().toString(), 
                "/queue/messages", 
                response
            );
            
            // 通知自己
            messagingTemplate.convertAndSendToUser(
                userId.toString(), 
                "/queue/messages", 
                response
            );
            
            logger.info("消息撤回成功: messageId={}", messageId);
            
        } catch (Exception e) {
            logger.error("撤回消息失败: {}", e.getMessage(), e);
        }
    }
}
