<template>
  <div class="chat-container">
    <!-- 会话列表侧边栏 -->
    <div class="conversations-sidebar">
      <div class="sidebar-header">
        <h2>消息</h2>
        <a-button type="primary" shape="circle" size="large">
          <template #icon><PlusOutlined /></template>
        </a-button>
      </div>

      <a-input-search
        v-model:value="searchText"
        placeholder="搜索会话..."
        class="search-input"
      />

      <div class="conversations-list">
        <div
          v-if="filteredConversations.length === 0"
          class="empty-state"
        >
          <a-empty description="暂无会话" />
        </div>

        <div
          v-for="conversation in filteredConversations"
          :key="`${conversation.userId}-${conversation.companionId}`"
          class="conversation-item"
          :class="{ active: isCurrentConversation(conversation) }"
          @click="selectConversation(conversation)"
        >
          <div class="avatar">
            {{ conversation.companionName.charAt(0) }}
          </div>
          <div class="conversation-info">
            <div class="name-row">
              <span class="name">{{ conversation.companionName }}</span>
              <span class="time">{{ formatTime(conversation.lastMessageTime) }}</span>
            </div>
            <div class="message-row">
              <span class="last-message">{{ conversation.lastMessage }}</span>
              <a-badge
                v-if="conversation.unreadCount > 0"
                :count="conversation.unreadCount"
                class="unread-badge"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 聊天主区域 -->
    <div class="chat-main">
      <div v-if="!currentConversation" class="empty-chat">
        <a-empty description="选择一个会话开始聊天" />
      </div>

      <div v-else class="chat-content">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="header-info">
            <h3>{{ currentConversation.companionName }}</h3>
            <span class="status" :class="{ online: isConnected }">
              {{ isConnected ? '在线' : '离线' }}
            </span>
          </div>
          <div class="header-actions">
            <a-button type="text" size="small">
              <template #icon><PhoneOutlined /></template>
            </a-button>
            <a-button type="text" size="small">
              <template #icon><VideoCameraOutlined /></template>
            </a-button>
            <a-button type="text" size="small">
              <template #icon><MoreOutlined /></template>
            </a-button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="messages-container">
          <div
            v-if="isLoading"
            class="loading-indicator"
          >
            <a-spin />
          </div>

          <div
            v-for="message in messages"
            :key="message.id"
            class="message-item"
            :class="{ 'from-me': isSentByMe(message) }"
          >
            <div class="message-avatar">
              {{ isSentByMe(message) ? '我' : currentConversation.companionName.charAt(0) }}
            </div>
            <div class="message-bubble">
              <div v-if="message.messageType === 'text'" class="message-text">
                {{ message.content }}
              </div>
              <div v-else-if="message.messageType === 'image'" class="message-image">
                <img :src="message.imageUrl" :alt="message.content" />
              </div>
              <div class="message-time">
                {{ formatDateTime(message.createTime) }}
              </div>
              <div v-if="message.isRead" class="message-status">✓✓</div>
            </div>
          </div>

          <!-- 输入状态提示 -->
          <div v-if="isTyping" class="typing-indicator">
            <span>{{ currentConversation.companionName }} 正在输入...</span>
          </div>

          <div ref="messagesEnd" />
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-toolbar">
            <a-button type="text" size="small">
              <template #icon><PictureOutlined /></template>
            </a-button>
            <a-button type="text" size="small">
              <template #icon><FileOutlined /></template>
            </a-button>
            <a-button type="text" size="small">
              <template #icon><SmileOutlined /></template>
            </a-button>
          </div>

          <div class="input-box">
            <a-textarea
              v-model:value="inputMessage"
              placeholder="输入消息..."
              :rows="3"
              :auto-size="{ minRows: 1, maxRows: 3 }"
              @keydown.enter.ctrl="handleSendMessage"
              @input="handleTyping"
            />
            <a-button
              type="primary"
              :loading="isSending"
              @click="handleSendMessage"
            >
              发送
            </a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useChatStore } from '../stores/chatStore';
import { formatDateTime } from '../utils/format';
import {
  PlusOutlined,
  PhoneOutlined,
  VideoCameraOutlined,
  MoreOutlined,
  PictureOutlined,
  FileOutlined,
  SmileOutlined,
} from '@ant-design/icons-vue';

const chatStore = useChatStore();
const inputMessage = ref('');
const searchText = ref('');
const isSending = ref(false);
const messagesEnd = ref<HTMLElement>();
const typingTimeout = ref<NodeJS.Timeout>();

// 从 chatStore 中获取响应式属性
const messages = computed(() => chatStore.messages);
const currentConversation = computed(() => chatStore.currentConversation);
const isConnected = computed(() => chatStore.isConnected);
const isLoading = computed(() => chatStore.isLoading);
const isTyping = computed(() => chatStore.isTyping);

// 计算属性
const filteredConversations = computed(() => {
  if (!searchText.value) {
    return chatStore.sortedConversations;
  }
  return chatStore.sortedConversations.filter((conv) =>
    conv.companionName.toLowerCase().includes(searchText.value.toLowerCase())
  );
});

// 方法
const formatTime = (dateString: string) => {
  const date = new Date(dateString);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;

  return date.toLocaleDateString();
};

const isCurrentConversation = (conversation: any) => {
  return (
    chatStore.currentConversation &&
    chatStore.currentConversation.companionId === conversation.companionId
  );
};

const selectConversation = (conversation: any) => {
  chatStore.setCurrentConversation(conversation);
};

const isSentByMe = (message: any) => {
  return message.senderId === chatStore.currentUserId;
};

const handleSendMessage = async () => {
  if (!inputMessage.value.trim() || !chatStore.currentConversation) return;

  isSending.value = true;
  try {
    chatStore.sendMessage(
      chatStore.currentConversation.companionId,
      inputMessage.value,
      'text'
    );
    inputMessage.value = '';
    await nextTick();
    scrollToBottom();
  } finally {
    isSending.value = false;
  }
};

const handleTyping = () => {
  if (!chatStore.currentConversation) return;

  // 清除之前的超时
  if (typingTimeout.value) {
    clearTimeout(typingTimeout.value);
  }

  // 发送输入状态
  chatStore.sendTyping(chatStore.currentConversation.companionId);

  // 3秒后停止输入状态
  typingTimeout.value = setTimeout(() => {
    chatStore.stopTyping(chatStore.currentConversation!.companionId);
  }, 3000);
};

const scrollToBottom = () => {
  nextTick(() => {
    messagesEnd.value?.scrollIntoView({ behavior: 'smooth' });
  });
};

// 生命周期
onMounted(async () => {
  const userInfo = localStorage.getItem('user');
  if (userInfo) {
    const user = JSON.parse(userInfo);
    try {
      await chatStore.initWebSocket(user.id, user.userType || 'user');
      await chatStore.loadConversations();
      await chatStore.loadUnreadMessages();
    } catch (error) {
      console.error('初始化聊天失败:', error);
    }
  }
});

onUnmounted(() => {
  chatStore.disconnect();
  if (typingTimeout.value) {
    clearTimeout(typingTimeout.value);
  }
});

// 监听消息变化，自动滚动到底部
watch(
  () => chatStore.messages.length,
  () => {
    scrollToBottom();
  }
);
</script>

<style lang="scss" scoped>
.chat-container {
  display: flex;
  height: 100vh;
  background: #f5f5f5;

  .conversations-sidebar {
    width: 300px;
    background: #fff;
    border-right: 1px solid #e8e8e8;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .sidebar-header {
      padding: 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid #e8e8e8;

      h2 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
      }
    }

    .search-input {
      margin: 12px;
    }

    .conversations-list {
      flex: 1;
      overflow-y: auto;

      .empty-state {
        padding: 40px 16px;
        text-align: center;
      }

      .conversation-item {
        padding: 12px 16px;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;
        display: flex;
        gap: 12px;
        transition: background-color 0.2s;

        &:hover {
          background-color: #f5f5f5;
        }

        &.active {
          background-color: #e6f7ff;
          border-left: 3px solid #1890ff;
        }

        .avatar {
          width: 48px;
          height: 48px;
          border-radius: 50%;
          background: #1890ff;
          color: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 600;
          flex-shrink: 0;
        }

        .conversation-info {
          flex: 1;
          min-width: 0;

          .name-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 4px;

            .name {
              font-weight: 500;
              color: #000;
            }

            .time {
              font-size: 12px;
              color: #999;
            }
          }

          .message-row {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .last-message {
              font-size: 12px;
              color: #666;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              flex: 1;
            }

            .unread-badge {
              margin-left: 8px;
            }
          }
        }
      }
    }
  }

  .chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: #fff;

    .empty-chat {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .chat-content {
      display: flex;
      flex-direction: column;
      height: 100%;

      .chat-header {
        padding: 16px;
        border-bottom: 1px solid #e8e8e8;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .header-info {
          h3 {
            margin: 0;
            font-size: 16px;
            font-weight: 600;
          }

          .status {
            font-size: 12px;
            color: #999;

            &.online {
              color: #52c41a;
            }
          }
        }

        .header-actions {
          display: flex;
          gap: 8px;
        }
      }

      .messages-container {
        flex: 1;
        overflow-y: auto;
        padding: 16px;
        display: flex;
        flex-direction: column;
        gap: 12px;

        .loading-indicator {
          text-align: center;
          padding: 20px;
        }

        .message-item {
          display: flex;
          gap: 8px;
          align-items: flex-end;

          &.from-me {
            flex-direction: row-reverse;

            .message-bubble {
              background: #1890ff;
              color: #fff;
            }
          }

          .message-avatar {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: #1890ff;
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 12px;
            font-weight: 600;
            flex-shrink: 0;
          }

          .message-bubble {
            max-width: 60%;
            background: #f0f0f0;
            border-radius: 8px;
            padding: 8px 12px;
            word-break: break-word;

            .message-text {
              font-size: 14px;
              line-height: 1.5;
            }

            .message-image {
              img {
                max-width: 200px;
                border-radius: 4px;
              }
            }

            .message-time {
              font-size: 11px;
              color: #999;
              margin-top: 4px;
            }

            .message-status {
              font-size: 11px;
              color: #999;
              margin-top: 2px;
            }
          }
        }

        .typing-indicator {
          font-size: 12px;
          color: #999;
          font-style: italic;
        }
      }

      .input-area {
        padding: 16px;
        border-top: 1px solid #e8e8e8;
        background: #fafafa;

        .input-toolbar {
          display: flex;
          gap: 8px;
          margin-bottom: 8px;
        }

        .input-box {
          display: flex;
          gap: 8px;

          :deep(.ant-input-textarea) {
            flex: 1;
          }

          button {
            align-self: flex-end;
          }
        }
      }
    }
  }
}
</style>
