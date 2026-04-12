package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.ChatMessageMapper;
import com.jcen.medpal.model.entity.ChatMessage;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.NotificationService;
import com.jcen.medpal.service.ChatMessageService;
import com.jcen.medpal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {
    
    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;
    
    @Override
    public ChatMessage sendMessage(ChatMessage message) {
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        this.save(message);
        createChatNotification(message);
        return message;
    }

    private void createChatNotification(ChatMessage message) {
        if (message == null || message.getReceiverId() == null || message.getSenderId() == null) {
            return;
        }
        if (message.getReceiverId().equals(message.getSenderId())) {
            return;
        }
        try {
            User sender = userService.getById(message.getSenderId());
            String senderName = sender != null && sender.getUserName() != null && !sender.getUserName().isEmpty()
                    ? sender.getUserName()
                    : "对方";
            String preview = "image".equals(message.getMessageType()) ? "[图片]" : message.getContent();
            if (preview == null || preview.isEmpty()) {
                preview = "[新消息]";
            }
            if (preview.length() > 40) {
                preview = preview.substring(0, 40) + "...";
            }
            Long relatedId = message.getOrderId();
            String relatedType = relatedId != null ? "order" : null;
            notificationService.createNotification(
                    message.getReceiverId(),
                    "system",
                    "您有一条新消息",
                    senderName + "：" + preview,
                    relatedType,
                    relatedId
            );
        } catch (Exception ignored) {
            // 聊天消息主流程优先，通知失败不影响发送
        }
    }
    
    @Override
    public List<ChatMessage> getChatHistory(Long userId, Long companionId, long current, long size) {
        return this.lambdaQuery()
                .and(wrapper -> wrapper
                    .eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, companionId)
                    .or()
                    .eq(ChatMessage::getSenderId, companionId).eq(ChatMessage::getReceiverId, userId))
                .eq(ChatMessage::getIsDelete, 0)
                .orderByAsc(ChatMessage::getCreateTime)
                .last("LIMIT " + (current - 1) * size + ", " + size)
                .list();
    }
    
    @Override
    public IPage<ChatMessage> getChatHistoryPage(Long userId, Long companionId, long current, long size) {
        Page<ChatMessage> page = new Page<>(current, size);
        return this.lambdaQuery()
                .and(wrapper -> wrapper
                    .eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, companionId)
                    .or()
                    .eq(ChatMessage::getSenderId, companionId).eq(ChatMessage::getReceiverId, userId))
                .eq(ChatMessage::getIsDelete, 0)
                .orderByAsc(ChatMessage::getCreateTime)
                .page(page);
    }
    
    @Override
    public IPage<ChatMessage> searchMessages(Long userId, String keyword, long current, long size) {
        Page<ChatMessage> page = new Page<>(current, size);
        return this.lambdaQuery()
                .and(wrapper -> wrapper
                    .eq(ChatMessage::getSenderId, userId)
                    .or()
                    .eq(ChatMessage::getReceiverId, userId))
                .and(wrapper -> wrapper
                    .like(ChatMessage::getContent, keyword)
                    .or()
                    .eq(ChatMessage::getMessageType, keyword))
                .eq(ChatMessage::getIsDelete, 0)
                .orderByDesc(ChatMessage::getCreateTime)
                .page(page);
    }
    
    @Override
    public List<ChatMessage> getUnreadMessages(Long userId) {
        return this.lambdaQuery()
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getIsRead, 0)
                .eq(ChatMessage::getIsDelete, 0)
                .orderByDesc(ChatMessage::getCreateTime)
                .list();
    }
    
    @Override
    public boolean markAsRead(Long id) {
        ChatMessage message = this.getById(id);
        if (message == null) {
            return false;
        }
        message.setIsRead(1);
        return this.updateById(message);
    }
    
    @Override
    public boolean markConversationAsRead(Long userId, Long companionId) {
        return this.lambdaUpdate()
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getSenderId, companionId)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1)
                .update();
    }
    
    @Override
    public List<Object> getConversations(Long userId) {
        List<Object> conversations = new ArrayList<>();
        
        // 查询与该用户相关的所有消息
        List<ChatMessage> messages = this.lambdaQuery()
                .and(wrapper -> wrapper
                    .eq(ChatMessage::getSenderId, userId)
                    .or()
                    .eq(ChatMessage::getReceiverId, userId))
                .eq(ChatMessage::getIsDelete, 0)
                .orderByDesc(ChatMessage::getCreateTime)
                .list();
        
        // 按对话方分组
        Map<Long, SimpleEntry<ChatMessage, Integer>> conversationMap = new HashMap<>();
        
        for (ChatMessage msg : messages) {
            Long companionId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            
            if (!conversationMap.containsKey(companionId)) {
                conversationMap.put(companionId, new SimpleEntry<>(msg, 0));
            }
            
            // 计算未读消息数
            if (msg.getReceiverId().equals(userId) && msg.getIsRead() != null && msg.getIsRead() == 0) {
                SimpleEntry<ChatMessage, Integer> entry = conversationMap.get(companionId);
                conversationMap.put(companionId, new SimpleEntry<>(entry.getKey(), entry.getValue() + 1));
            }
        }
        
        // 转换为会话列表
        for (Map.Entry<Long, SimpleEntry<ChatMessage, Integer>> entry : conversationMap.entrySet()) {
            Map<String, Object> conversation = new HashMap<>();
            Long companionId = entry.getKey();
            ChatMessage lastMsg = entry.getValue().getKey();
            int unreadCount = entry.getValue().getValue();
            
            conversation.put("companionId", companionId);
            conversation.put("lastMessage", lastMsg.getContent());
            conversation.put("lastMessageTime", lastMsg.getCreateTime() != null ? lastMsg.getCreateTime().toString() : "");
            conversation.put("unreadCount", unreadCount);
            
            // 获取对方信息
            try {
                var companion = userService.getById(companionId);
                if (companion != null) {
                    conversation.put("companionName", companion.getUserName());
                    conversation.put("companionAvatar", companion.getUserAvatar());
                }
            } catch (Exception e) {
                conversation.put("companionName", "用户" + companionId);
            }
            
            conversations.add(conversation);
        }
        
        return conversations;
    }
    
    @Override
    public boolean deleteMessage(Long messageId, Long userId) {
        ChatMessage message = this.getById(messageId);
        if (message == null || !message.getSenderId().equals(userId)) {
            return false;
        }
        
        message.setIsDelete(1);
        return this.updateById(message);
    }
    
    @Override
    public boolean deleteConversation(Long userId, Long companionId) {
        return this.lambdaUpdate()
                .and(wrapper -> wrapper
                    .eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, companionId)
                    .or()
                    .eq(ChatMessage::getSenderId, companionId).eq(ChatMessage::getReceiverId, userId))
                .set(ChatMessage::getIsDelete, 1)
                .update();
    }
    
    @Override
    public boolean recallMessage(Long messageId, Long userId, String reason) {
        ChatMessage message = this.getById(messageId);
        if (message == null) {
            return false;
        }
        
        // 检查是否是消息发送者
        if (!message.getSenderId().equals(userId)) {
            return false;
        }
        
        // 检查是否在撤回时间范围内（2分钟内）
        if (!canRecall(messageId, userId)) {
            return false;
        }
        
        // 执行撤回
        message.setIsRecalled(1);
        message.setRecalledTime(LocalDateTime.now());
        message.setRecallReason(reason);
        message.setContent("【消息已撤回】");
        
        return this.updateById(message);
    }
    
    @Override
    public boolean canRecall(Long messageId, Long userId) {
        ChatMessage message = this.getById(messageId);
        if (message == null || !message.getSenderId().equals(userId)) {
            return false;
        }
        
        // 检查是否已被撤回
        if (message.getIsRecalled() != null && message.getIsRecalled() == 1) {
            return false;
        }
        
        // 检查是否在2分钟内
        if (message.getCreateTime() == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recallDeadline = message.getCreateTime().plusMinutes(2);
        
        return now.isBefore(recallDeadline);
    }
}
