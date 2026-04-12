<template>
  <view class="page">
    <view v-if="!peerId" class="empty-panel">
      <uni-icons type="chat" size="42" color="#9aa4b8"></uni-icons>
      <text class="empty-title">未找到聊天对象</text>
      <text class="empty-desc">请从订单详情页点击“联系”进入聊天</text>
      <button class="order-btn" @tap="goOrders">去订单页</button>
    </view>

    <view v-else class="chat-container">
      <scroll-view
        class="message-list"
        scroll-y
        :scroll-top="scrollTop"
        scroll-with-animation
      >
        <view v-if="loading && !messages.length" class="loading-box">
          <uni-load-more status="loading" />
        </view>

        <view v-else-if="!messages.length" class="empty-box">
          <text>当前还没有消息，发送第一条吧</text>
        </view>

        <view v-else>
          <view
            v-for="item in messages"
            :key="item.id"
            :id="`msg-${item.id}`"
            class="message-row"
            :class="item.sender === 'me' ? 'from-me' : 'from-other'"
            @longpress="onMessageLongPress(item)"
          >
            <image
              v-if="messageAvatar(item)"
              class="avatar-image"
              mode="aspectFill"
              :src="messageAvatar(item)"
            />
            <view v-else class="avatar-fallback">{{ messageAvatarInitial(item) }}</view>
            <view class="message-main">
              <text class="message-name">{{ messageDisplayName(item) }}</text>
              <view class="bubble">
                <text v-if="isRecalledMessage(item)" class="recall-text">{{ item.content }}</text>
                <text v-else-if="item.messageType !== 'image'" class="message-text">{{ item.content }}</text>
                <image
                  v-else
                  class="message-image"
                  mode="aspectFill"
                  :src="item.imageUrl"
                  @load="onMessageImageLoad"
                  @tap="previewImage(item.imageUrl)"
                />
                <text class="time">{{ formatTime(item.createTime) }}</text>
              </view>
            </view>
          </view>
        </view>

        <view id="chat-bottom" class="bottom-anchor"></view>
      </scroll-view>

      <view class="input-bar">
        <button class="img-btn" :disabled="uploading || sending" @tap="chooseAndSendImage">
          {{ uploading ? '上传中' : '图片' }}
        </button>
        <uni-easyinput
          v-model="inputMessage"
          class="input"
          :clearable="false"
          placeholder="输入消息..."
          @confirm="sendTextMessage"
        />
        <button class="send-btn" :disabled="sending || !inputMessage.trim()" @tap="sendTextMessage">
          发送
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { formatDateTime, normalizeFileUrl as resolveFileUrl } from '@/utils/format.js';
import { BASE_URL, getToken, request } from '@/utils/request.js';

export default {
  data() {
    return {
      loading: false,
      sending: false,
      uploading: false,
      recalling: false,
      inputMessage: '',
      scrollTop: 0,
      messages: [],
      pollTimer: null,
      pollInterval: 3000,
      currentUserId: null,
      currentUserName: '我',
      currentUserAvatar: '',
      senderType: 'user',
      peerId: null,
      peerName: '对方',
      peerAvatar: '',
      orderId: null
    };
  },
  onLoad(options) {
    this.initFromOptions(options || {});
    this.initChat();
  },
  onShow() {
    if (this.currentUserId && this.peerId) {
      this.startPolling();
    }
  },
  onHide() {
    this.stopPolling();
  },
  onUnload() {
    this.stopPolling();
  },
  methods: {
    async requestChat(path, options = {}) {
      return request({
        url: `/chat${path}`,
        ...options
      });
    },
    initFromOptions(options) {
      const peerIdRaw = options.peerId || options.companionId || options.userId;
      const parsedPeerId = Number(peerIdRaw);
      this.peerId = Number.isFinite(parsedPeerId) && parsedPeerId > 0 ? parsedPeerId : null;
      const rawPeerName = options.peerName || options.companionName || options.userName || '对方';
      try {
        this.peerName = decodeURIComponent(rawPeerName);
      } catch (error) {
        this.peerName = rawPeerName;
      }
      this.syncNavTitle();
      const rawPeerAvatar = options.peerAvatar || options.companionAvatar || options.userAvatar || '';
      try {
        this.peerAvatar = decodeURIComponent(rawPeerAvatar);
      } catch (error) {
        this.peerAvatar = rawPeerAvatar;
      }
      const parsedOrderId = Number(options.orderId);
      this.orderId = Number.isFinite(parsedOrderId) && parsedOrderId > 0 ? parsedOrderId : null;
    },
    syncNavTitle() {
      const title = this.peerName || '聊天';
      uni.setNavigationBarTitle({ title });
    },
    async initChat() {
      const user = uni.getStorageSync('userInfo');
      if (!user?.id) {
        uni.showToast({ title: '请先登录', icon: 'none' });
        uni.reLaunch({ url: '/pages/login/index' });
        return;
      }

      this.currentUserId = Number(user.id);
      this.currentUserName = user.userName || '我';
      this.currentUserAvatar = user.userAvatar || '';
      this.senderType = user.userRole === 'companion' ? 'companion' : 'user';

      if (!this.peerId) return;

      await this.fetchMessages(true);
      this.startPolling();
    },
    startPolling() {
      if (!this.peerId) return;
      this.stopPolling();
      this.pollTimer = setInterval(() => {
        this.fetchMessages();
      }, this.pollInterval);
    },
    stopPolling() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer);
        this.pollTimer = null;
      }
    },
    async fetchMessages(showLoading = false) {
      if (!this.currentUserId || !this.peerId) return;

      if (showLoading) this.loading = true;
      try {
        const page = await this.requestChat('/history', {
          method: 'GET',
          params: {
            userId: this.currentUserId,
            companionId: this.peerId,
            current: 1,
            size: 100
          },
          showLoading: false
        });

        const records = page?.records || [];
        if (!this.orderId && records.length) {
          const latestOrderId = records[records.length - 1]?.orderId;
          const parsed = Number(latestOrderId);
          this.orderId = Number.isFinite(parsed) && parsed > 0 ? parsed : this.orderId;
        }

        const oldLastId = this.messages.length ? this.messages[this.messages.length - 1].id : null;
        this.messages = records.map((item) => this.normalizeMessage(item));

        const newLastId = this.messages.length ? this.messages[this.messages.length - 1].id : null;
        if (showLoading || oldLastId !== newLastId) {
          this.scrollToBottom();
        }

        this.markConversationRead();
      } catch (error) {
        console.error('获取聊天记录失败', error);
      } finally {
        this.loading = false;
      }
    },
    normalizeMessage(item) {
      const type = item.messageType || (item.imageUrl ? 'image' : 'text');
      return {
        id: item.id || Date.now(),
        sender: Number(item.senderId) === this.currentUserId ? 'me' : 'other',
        content: item.content || '',
        messageType: type,
        imageUrl: this.normalizeFileUrl(item.imageUrl),
        createTime: item.createTime
      };
    },
    messageDisplayName(item) {
      return item?.sender === 'me' ? this.currentUserName : this.peerName;
    },
    messageAvatar(item) {
      const raw = item?.sender === 'me' ? this.currentUserAvatar : this.peerAvatar;
      if (!raw) return '';
      return this.normalizeFileUrl(raw);
    },
    messageAvatarInitial(item) {
      const name = this.messageDisplayName(item) || '对方';
      return name.slice(0, 1);
    },
    isRecalledMessage(item) {
      return item?.content === '【消息已撤回】';
    },
    onMessageLongPress(item) {
      if (!item || item.sender !== 'me' || !item.id) return;
      if (this.recalling) return;
      if (this.isRecalledMessage(item)) {
        uni.showToast({ title: '该消息已撤回', icon: 'none' });
        return;
      }

      uni.showActionSheet({
        itemList: ['撤回消息'],
        success: (res) => {
          if (res.tapIndex === 0) {
            this.confirmRecall(item);
          }
        }
      });
    },
    async confirmRecall(item) {
      if (this.recalling) return;
      this.recalling = true;
      try {
        const canRecall = await this.requestChat(`/can-recall/${item.id}`, {
          method: 'GET',
          params: {
            userId: this.currentUserId
          },
          showLoading: false
        });
        if (!canRecall) {
          uni.showToast({ title: '消息超过2分钟，无法撤回', icon: 'none' });
          return;
        }

        uni.showModal({
          title: '撤回消息',
          content: '确认撤回这条消息吗？',
          success: async (res) => {
            if (!res.confirm) return;
            try {
              await this.requestChat(`/recall/${item.id}`, {
                method: 'POST',
                params: {
                  userId: this.currentUserId,
                  reason: '用户撤回'
                },
                showLoading: false
              });
              await this.fetchMessages();
              uni.showToast({ title: '已撤回', icon: 'success' });
            } catch (error) {
              uni.showToast({ title: error.message || '撤回失败', icon: 'none' });
            }
          },
          complete: () => {
            this.recalling = false;
          }
        });
        return;
      } catch (error) {
        uni.showToast({ title: error.message || '撤回失败', icon: 'none' });
      } finally {
        if (this.recalling) {
          this.recalling = false;
        }
      }
    },
    normalizeFileUrl(url) {
      return resolveFileUrl(url);
    },
    async markConversationRead() {
      try {
        await this.requestChat('/conversation/read', {
          method: 'POST',
          params: {
            userId: this.currentUserId,
            companionId: this.peerId
          },
          showLoading: false
        });
      } catch (error) {
        // ignore
      }
    },
    ensureCanSend() {
      if (!this.peerId) {
        uni.showToast({ title: '未找到聊天对象', icon: 'none' });
        return false;
      }
      if (!this.orderId) {
        uni.showToast({ title: '请从订单详情进入聊天', icon: 'none' });
        return false;
      }
      return true;
    },
    async sendTextMessage() {
      const content = this.inputMessage.trim();
      if (!content || this.sending) return;
      if (!this.ensureCanSend()) return;

      this.sending = true;
      try {
        await this.requestChat('/send', {
          method: 'POST',
          data: {
            orderId: this.orderId,
            senderId: this.currentUserId,
            senderType: this.senderType,
            receiverId: this.peerId,
            messageType: 'text',
            content
          },
          showLoading: false
        });

        this.inputMessage = '';
        await this.fetchMessages();
      } catch (error) {
        uni.showToast({ title: error.message || '发送失败', icon: 'none' });
      } finally {
        this.sending = false;
      }
    },
    chooseAndSendImage() {
      if (this.uploading || this.sending) return;
      if (!this.ensureCanSend()) return;

      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          const file = res.tempFiles?.[0];
          if (!file?.path) return;
          if (file.size > 1024 * 1024) {
            uni.showToast({ title: '图片需小于1MB', icon: 'none' });
            return;
          }
          await this.uploadAndSendImage(file.path);
        }
      });
    },
    uploadAndSendImage(filePath) {
      this.uploading = true;
      return new Promise((resolve) => {
        uni.uploadFile({
          url: `${BASE_URL}/file/upload`,
          filePath,
          name: 'file',
          formData: {
            biz: 'user_avatar'
          },
          header: {
            ...(getToken() ? { Authorization: `Bearer ${getToken()}` } : {})
          },
          success: async (uploadRes) => {
            try {
              const parsed = JSON.parse(uploadRes.data || '{}');
              if (parsed.code !== 0 || !parsed.data) {
                throw new Error(parsed.message || '上传失败');
              }

              await this.requestChat('/send', {
                method: 'POST',
                data: {
                  orderId: this.orderId,
                  senderId: this.currentUserId,
                  senderType: this.senderType,
                  receiverId: this.peerId,
                  messageType: 'image',
                  content: '[图片]',
                  imageUrl: parsed.data
                },
                showLoading: false
              });

              await this.fetchMessages();
            } catch (error) {
              uni.showToast({ title: error.message || '发送图片失败', icon: 'none' });
            } finally {
              this.uploading = false;
              resolve();
            }
          },
          fail: () => {
            this.uploading = false;
            uni.showToast({ title: '上传失败', icon: 'none' });
            resolve();
          }
        });
      });
    },
    previewImage(url) {
      if (!url) return;
      uni.previewImage({
        urls: [url],
        current: url
      });
    },
    onMessageImageLoad() {
      this.scrollToBottom();
    },
    formatTime(value) {
      return formatDateTime(value);
    },
    scrollToBottom() {
      this.$nextTick(() => {
        // 使用 scroll-top 强制向下滚动，避免锚点模式在部分端不触发
        this.scrollTop += 100000;
        setTimeout(() => {
          this.scrollTop += 100000;
        }, 80);
      });
    },
    goOrders() {
      uni.switchTab({ url: '/pages/order/index' });
    }
  }
};
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f4f6fb;
}

.empty-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
  text-align: center;
}

.empty-title {
  margin-top: 14rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #2d3950;
}

.empty-desc {
  margin-top: 10rpx;
  font-size: 23rpx;
  color: #7b8291;
}

.order-btn {
  margin-top: 20rpx;
  width: 260rpx;
  border-radius: 999rpx;
  background: #2f65f9;
  color: #ffffff;
  font-size: 24rpx;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.message-list {
  flex: 1;
  min-height: 0;
  padding: 20rpx 24rpx;
  padding-bottom: 150rpx;
  box-sizing: border-box;
}

.loading-box,
.empty-box {
  padding: 120rpx 0;
  text-align: center;
  color: #9aa4b8;
  font-size: 23rpx;
}

.message-row {
  display: flex;
  width: 100%;
  align-items: flex-start;
  gap: 10rpx;
  margin-bottom: 16rpx;
}

.message-row.from-me {
  justify-content: flex-start;
  flex-direction: row-reverse;
}

.message-row.from-other {
  justify-content: flex-start;
}

.avatar-image,
.avatar-fallback {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.avatar-image {
  background: #e8ecf8;
}

.avatar-fallback {
  background: #dce6ff;
  color: #2f65f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
}

.message-main {
  max-width: 520rpx;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.from-me .message-main {
  align-items: flex-end;
}

.message-name {
  font-size: 20rpx;
  color: #7b8291;
  margin: 0 4rpx 6rpx;
}

.bubble {
  max-width: 100%;
  border-radius: 16rpx;
  padding: 14rpx 16rpx;
  background: #ffffff;
  box-shadow: 0 6rpx 20rpx rgba(33, 56, 100, 0.06);
}

.from-me .bubble {
  background: #2f65f9;
}

.message-text {
  font-size: 26rpx;
  line-height: 1.5;
  color: #2d3950;
  word-break: break-all;
}

.from-me .message-text {
  color: #ffffff;
}

.recall-text {
  font-size: 24rpx;
  color: #7b8291;
}

.message-image {
  width: 280rpx;
  height: 280rpx;
  border-radius: 12rpx;
  background: #eef2ff;
}

.time {
  display: block;
  margin-top: 8rpx;
  font-size: 20rpx;
  color: #9aa4b8;
}

.from-me .time {
  color: rgba(255, 255, 255, 0.72);
}

.bottom-anchor {
  height: 2rpx;
}

.input-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 14rpx 16rpx;
  padding-bottom: calc(14rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(14rpx + env(safe-area-inset-bottom));
  background: #ffffff;
  border-top: 1rpx solid #edf0f6;
  box-sizing: border-box;
}

.img-btn {
  width: 110rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 12rpx;
  background: #eef2ff;
  color: #2f65f9;
  font-size: 22rpx;
}

.input {
  flex: 1;
}

.send-btn {
  width: 110rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 12rpx;
  background: #2f65f9;
  color: #ffffff;
  font-size: 24rpx;
}

button[disabled] {
  opacity: 0.55;
}
</style>
