import request from '../request';
import { BaseResponse, PageData, PageParams } from '../types';

// 聊天消息
export interface ChatMessage {
  id: number;
  senderId: number;
  senderType: 'user' | 'companion';
  receiverId: number;
  receiverType: 'user' | 'companion';
  content: string;
  messageType: 'text' | 'image' | 'file';
  isRead: boolean;
  createTime: string;
}

// 会话信息
export interface Conversation {
  userId: number;
  companionId: number;
  userName: string;
  companionName: string;
  lastMessage: string;
  lastMessageTime: string;
  unreadCount: number;
}

/**
 * 发送消息
 * @param message 消息内容
 */
export function sendMessage(message: Partial<ChatMessage>): Promise<BaseResponse<ChatMessage>> {
  return request.post('/chat/send', message) as Promise<BaseResponse<ChatMessage>>;
}

/**
 * 获取聊天历史
 * @param userId 用户ID
 * @param companionId 陪诊员ID
 * @param current 当前页码
 * @param size 每页大小
 */
export function getChatHistory(
  userId: number,
  companionId: number,
  current: number = 1,
  size: number = 20
): Promise<BaseResponse<ChatMessage[]>> {
  return request.get('/chat/history', {
    params: { userId, companionId, current, size },
  }) as Promise<BaseResponse<ChatMessage[]>>;
}

/**
 * 获取未读消息
 * @param userId 用户ID
 */
export function getUnreadMessages(userId: number): Promise<BaseResponse<ChatMessage[]>> {
  return request.get('/chat/unread', {
    params: { userId },
  }) as Promise<BaseResponse<ChatMessage[]>>;
}

/**
 * 标记消息为已读
 * @param id 消息ID
 */
export function markAsRead(id: number): Promise<BaseResponse<string>> {
  return request.post(`/chat/read/${id}`) as Promise<BaseResponse<string>>;
}

/**
 * 获取会话列表
 * @param userId 用户ID
 */
export function getConversations(userId: number): Promise<BaseResponse<Conversation[]>> {
  return request.get('/chat/conversations', {
    params: { userId },
  }) as Promise<BaseResponse<Conversation[]>>;
}
