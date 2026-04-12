import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import websocketService, { WebSocketMessage } from '../services/websocket';
import { getChatHistory, getConversations, getUnreadMessages, markAsRead } from '../api/modules/chat';
import { WS_BASE_URL } from '../config/service';

export interface ChatMessage {
  id: number;
  orderId: number;
  senderId: number;
  senderType: 'user' | 'companion';
  receiverId: number;
  messageType: 'text' | 'image' | 'file';
  content: string;
  imageUrl?: string;
  isRead: boolean;
  createTime: string;
  isRecalled?: boolean;
}

export interface Conversation {
  userId: number;
  companionId: number;
  userName: string;
  companionName: string;
  lastMessage: string;
  lastMessageTime: string;
  unreadCount: number;
}

export const useChatStore = defineStore('chat', () => {
  // 状态
  const messages = ref<ChatMessage[]>([]);
  const conversations = ref<Conversation[]>([]);
  const currentConversation = ref<Conversation | null>(null);
  const isConnected = ref(false);
  const isLoading = ref(false);
  const typingUsers = ref<Set<number>>(new Set());
  const currentUserId = ref<number | null>(null);
  const currentUserType = ref<'user' | 'companion'>('user');

  // 计算属性
  const unreadCount = computed(() => {
    return conversations.value.reduce((sum, conv) => sum + conv.unreadCount, 0);
  });

  const sortedConversations = computed(() => {
    return [...conversations.value].sort((a, b) => {
      return new Date(b.lastMessageTime).getTime() - new Date(a.lastMessageTime).getTime();
    });
  });

  const isTyping = computed(() => typingUsers.value.size > 0);

  // 方法
  const initWebSocket = async (userId: number, userType: 'user' | 'companion' = 'user') => {
    currentUserId.value = userId;
    currentUserType.value = userType;

    const token = localStorage.getItem('token') || '';
    const wsUrl = WS_BASE_URL;

    try {
      await websocketService.connect({
        url: wsUrl,
        token,
        userId,
        userType,
        onConnect: () => {
          isConnected.value = true;
          console.log('WebSocket 已连接');
        },
        onDisconnect: () => {
          isConnected.value = false;
          console.log('WebSocket 已断开');
        },
        onError: (error) => {
          console.error('WebSocket 错误:', error);
        },
        onMessage: (message: WebSocketMessage) => {
          handleWebSocketMessage(message);
        },
      });

      // 注册特定类型的消息处理器
      websocketService.on('chat', handleChatMessage);
      websocketService.on('read', handleReadMessage);
      websocketService.on('typing', handleTypingMessage);
      websocketService.on('stopTyping', handleStopTypingMessage);
      websocketService.on('recall', handleRecallMessage);
    } catch (error) {
      console.error('WebSocket 连接失败:', error);
      throw error;
    }
  };

  const handleWebSocketMessage = (message: WebSocketMessage) => {
    switch (message.type) {
      case 'chat':
        handleChatMessage(message);
        break;
      case 'read':
        handleReadMessage(message);
        break;
      case 'typing':
        handleTypingMessage(message);
        break;
      case 'stopTyping':
        handleStopTypingMessage(message);
        break;
      case 'recall':
        handleRecallMessage(message);
        break;
    }
  };

  const handleChatMessage = (message: WebSocketMessage) => {
    if (message.message) {
      const chatMsg: ChatMessage = {
        id: message.message.id,
        orderId: message.message.orderId,
        senderId: message.message.senderId,
        senderType: message.message.senderType,
        receiverId: message.message.receiverId,
        messageType: message.message.messageType,
        content: message.message.content,
        imageUrl: message.message.imageUrl,
        isRead: message.message.isRead === 1,
        createTime: message.message.createTime,
      };

      // 添加到消息列表
      messages.value.push(chatMsg);

      // 如果是接收到的消息，自动标记为已读
      if (chatMsg.receiverId === currentUserId.value && !chatMsg.isRead) {
        markAsRead(chatMsg.id);
        websocketService.markAsRead(chatMsg.id);
      }

      // 更新会话列表
      updateConversationLastMessage(chatMsg);
    }
  };

  const handleReadMessage = (message: WebSocketMessage) => {
    if (message.messageId) {
      const msg = messages.value.find((m) => m.id === message.messageId);
      if (msg) {
        msg.isRead = true;
      }
    }
  };

  const handleTypingMessage = (message: WebSocketMessage) => {
    if (message.senderId) {
      typingUsers.value.add(message.senderId);
    }
  };

  const handleStopTypingMessage = (message: WebSocketMessage) => {
    if (message.senderId) {
      typingUsers.value.delete(message.senderId);
    }
  };

  const handleRecallMessage = (message: WebSocketMessage) => {
    if (message.messageId) {
      const msg = messages.value.find((m) => m.id === message.messageId);
      if (msg) {
        msg.isRecalled = true;
        msg.content = '【消息已撤回】';
      }
    }
  };

  const updateConversationLastMessage = (message: ChatMessage) => {
    const conversationId = message.senderId === currentUserId.value ? message.receiverId : message.senderId;
    const conversation = conversations.value.find(
      (c) => (c.userId === currentUserId.value && c.companionId === conversationId) ||
             (c.companionId === currentUserId.value && c.userId === conversationId)
    );

    if (conversation) {
      conversation.lastMessage = message.content;
      conversation.lastMessageTime = message.createTime;
      if (message.receiverId === currentUserId.value && !message.isRead) {
        conversation.unreadCount++;
      }
    }
  };

  const loadConversations = async () => {
    if (!currentUserId.value) return;

    isLoading.value = true;
    try {
      const response = await getConversations(currentUserId.value);
      if (response.code === 0) {
        conversations.value = response.data || [];
      }
    } catch (error) {
      console.error('加载会话列表失败:', error);
    } finally {
      isLoading.value = false;
    }
  };

  const loadChatHistory = async (companionId: number, current: number = 1, size: number = 20) => {
    if (!currentUserId.value) return;

    isLoading.value = true;
    try {
      const response = await getChatHistory(currentUserId.value, companionId, current, size);
      if (response.code === 0) {
        const newMessages = response.data || [];
        if (current === 1) {
          messages.value = newMessages;
        } else {
          messages.value = [...newMessages, ...messages.value];
        }
      }
    } catch (error) {
      console.error('加载聊天历史失败:', error);
    } finally {
      isLoading.value = false;
    }
  };

  const loadUnreadMessages = async () => {
    if (!currentUserId.value) return;

    try {
      const response = await getUnreadMessages(currentUserId.value);
      if (response.code === 0) {
        const unreadMessages = response.data || [];
        messages.value.push(...unreadMessages);
      }
    } catch (error) {
      console.error('加载未读消息失败:', error);
    }
  };

  const sendMessage = (receiverId: number, content: string, messageType: 'text' | 'image' = 'text', imageUrl?: string) => {
    websocketService.sendMessage(receiverId, content, messageType, imageUrl);
  };

  const sendTyping = (receiverId: number) => {
    websocketService.sendTyping(receiverId);
  };

  const stopTyping = (receiverId: number) => {
    websocketService.stopTyping(receiverId);
  };

  const recallMessage = (messageId: number) => {
    websocketService.recallMessage(messageId);
  };

  const setCurrentConversation = (conversation: Conversation) => {
    currentConversation.value = conversation;
    messages.value = [];
    loadChatHistory(conversation.companionId);
  };

  const disconnect = () => {
    websocketService.disconnect();
    isConnected.value = false;
  };

  return {
    // 状态
    messages,
    conversations,
    currentConversation,
    isConnected,
    isLoading,
    typingUsers,
    currentUserId,
    currentUserType,

    // 计算属性
    unreadCount,
    sortedConversations,
    isTyping,

    // 方法
    initWebSocket,
    loadConversations,
    loadChatHistory,
    loadUnreadMessages,
    sendMessage,
    sendTyping,
    stopTyping,
    recallMessage,
    setCurrentConversation,
    disconnect,
  };
});
