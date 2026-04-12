package com.jcen.medpal.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.ChatMessage;
import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {
    
    ChatMessage sendMessage(ChatMessage message);
    
    List<ChatMessage> getChatHistory(Long userId, Long companionId, long current, long size);
    
    IPage<ChatMessage> getChatHistoryPage(Long userId, Long companionId, long current, long size);
    
    IPage<ChatMessage> searchMessages(Long userId, String keyword, long current, long size);
    
    List<ChatMessage> getUnreadMessages(Long userId);
    
    boolean markAsRead(Long id);
    
    boolean markConversationAsRead(Long userId, Long companionId);
    
    List<Object> getConversations(Long userId);
    
    boolean deleteMessage(Long messageId, Long userId);
    
    boolean deleteConversation(Long userId, Long companionId);
    
    // 撤回消息
    boolean recallMessage(Long messageId, Long userId, String reason);
    
    // 检查是否可以撤回（2分钟内）
    boolean canRecall(Long messageId, Long userId);
}
