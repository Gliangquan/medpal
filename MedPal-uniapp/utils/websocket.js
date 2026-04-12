/**
 * uni-app WebSocket 工具类
 * 支持 STOMP 协议的实时聊天
 */

const resolveWsUrl = () => {
  if (typeof window !== 'undefined' && window.location?.host) {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${protocol}//${window.location.host}/api/ws`;
  }
  return 'ws://127.0.0.1:9901/api/ws';
};

const WS_URL = resolveWsUrl();
const TOKEN_KEY = 'medpal_token';
const USER_ID_KEY = 'medpal_userId';
const USER_TYPE_KEY = 'medpal_userType';

class WebSocketService {
  constructor() {
    this.ws = null;
    this.isConnected = false;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 3000;
    this.messageHandlers = new Map();
    this.messageQueue = [];
    this.subscriptions = new Map();
    this.stompConnected = false;
    this.heartbeatTimer = null;
  }

  /**
   * 连接 WebSocket
   */
  connect(options = {}) {
    return new Promise((resolve, reject) => {
      try {
        const token = uni.getStorageSync(TOKEN_KEY);
        const userId = uni.getStorageSync(USER_ID_KEY);
        const userType = uni.getStorageSync(USER_TYPE_KEY) || 'user';

        if (!token || !userId) {
          reject(new Error('未登录'));
          return;
        }

        const wsUrl = `${WS_URL}?userId=${userId}&token=${token}`;

        uni.connectSocket({
          url: wsUrl,
          complete: () => {}
        });

        uni.onSocketOpen(() => {
          console.log('WebSocket 已连接');
          this.isConnected = true;
          this.reconnectAttempts = 0;

          // 发送 STOMP CONNECT 帧
          this.sendStompConnect(token);

          // 启动心跳
          this.startHeartbeat();

          options.onConnect?.();
          resolve();
        });

        uni.onSocketError((error) => {
          console.error('WebSocket 错误:', error);
          this.isConnected = false;
          options.onError?.(error);
          reject(error);
        });

        uni.onSocketMessage((event) => {
          this.handleMessage(event.data);
        });

        uni.onSocketClose(() => {
          console.log('WebSocket 已断开');
          this.isConnected = false;
          this.stompConnected = false;
          this.stopHeartbeat();
          options.onDisconnect?.();
          this.attemptReconnect(options);
        });
      } catch (error) {
        reject(error);
      }
    });
  }

  /**
   * 发送 STOMP CONNECT 帧
   */
  sendStompConnect(token) {
    const frame = `CONNECT
accept-version:1.0,1.1,1.2
heart-beat:30000,30000
Authorization:Bearer ${token}

\0`;
    this.send(frame);
  }

  /**
   * 处理接收到的消息
   */
  handleMessage(data) {
    // 解析 STOMP 帧
    if (data.includes('CONNECTED')) {
      this.stompConnected = true;
      this.subscribeToMessages();
      this.flushMessageQueue();
    } else if (data.includes('MESSAGE')) {
      this.handleStompMessage(data);
    } else if (data.includes('RECEIPT')) {
      // 处理收据
    }
  }

  /**
   * 处理 STOMP 消息
   */
  handleStompMessage(frameData) {
    try {
      const lines = frameData.split('\n');
      let bodyStart = -1;

      for (let i = 0; i < lines.length; i++) {
        if (lines[i].trim() === '') {
          bodyStart = i + 1;
          break;
        }
      }

      if (bodyStart > 0) {
        const body = lines.slice(bodyStart).join('\n').replace(/\0/g, '');
        const message = JSON.parse(body);
        this.handleWebSocketMessage(message);
      }
    } catch (error) {
      console.error('解析消息失败:', error);
    }
  }

  /**
   * 处理 WebSocket 消息
   */
  handleWebSocketMessage(message) {
    const handler = this.messageHandlers.get(message.type);
    if (handler) {
      handler(message);
    }
  }

  /**
   * 订阅消息队列
   */
  subscribeToMessages() {
    const userId = uni.getStorageSync(USER_ID_KEY);
    const destination = `/user/${userId}/queue/messages`;

    const frame = `SUBSCRIBE
id:sub-${userId}
destination:${destination}

\0`;
    this.send(frame);
    this.subscriptions.set(destination, true);
  }

  /**
   * 发送消息
   */
  sendMessage(receiverId, content, messageType = 'text', imageUrl = null) {
    if (!this.isConnected || !this.stompConnected) {
      this.messageQueue.push({ receiverId, content, messageType, imageUrl });
      return;
    }

    const orderId = uni.getStorageSync('currentOrderId') || 0;
    const message = {
      orderId,
      content,
      messageType,
      imageUrl
    };

    const frame = `SEND
destination:/app/chat.send/${receiverId}
content-type:application/json

${JSON.stringify(message)}\0`;
    this.send(frame);
  }

  /**
   * 标记消息已读
   */
  markAsRead(messageId) {
    if (!this.isConnected || !this.stompConnected) return;

    const frame = `SEND
destination:/app/chat.read/${messageId}

\0`;
    this.send(frame);
  }

  /**
   * 发送输入状态
   */
  sendTyping(receiverId) {
    if (!this.isConnected || !this.stompConnected) return;

    const frame = `SEND
destination:/app/chat.typing/${receiverId}

\0`;
    this.send(frame);
  }

  /**
   * 停止输入状态
   */
  stopTyping(receiverId) {
    if (!this.isConnected || !this.stompConnected) return;

    const frame = `SEND
destination:/app/chat.stopTyping/${receiverId}

\0`;
    this.send(frame);
  }

  /**
   * 撤回消息
   */
  recallMessage(messageId) {
    if (!this.isConnected || !this.stompConnected) return;

    const frame = `SEND
destination:/app/chat.recall/${messageId}

\0`;
    this.send(frame);
  }

  /**
   * 发送原始数据
   */
  send(data) {
    if (this.isConnected) {
      uni.sendSocketMessage({
        data,
        success: () => {
          console.log('消息已发送');
        },
        fail: (error) => {
          console.error('发送失败:', error);
        }
      });
    }
  }

  /**
   * 注册消息处理器
   */
  on(type, handler) {
    this.messageHandlers.set(type, handler);
  }

  /**
   * 移除消息处理器
   */
  off(type) {
    this.messageHandlers.delete(type);
  }

  /**
   * 处理消息队列
   */
  flushMessageQueue() {
    while (this.messageQueue.length > 0) {
      const msg = this.messageQueue.shift();
      this.sendMessage(msg.receiverId, msg.content, msg.messageType, msg.imageUrl);
    }
  }

  /**
   * 启动心跳
   */
  startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      if (this.isConnected && this.stompConnected) {
        this.send('\n');
      }
    }, 30000);
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 尝试重新连接
   */
  attemptReconnect(options) {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('WebSocket 重连失败，已达到最大重试次数');
      return;
    }

    this.reconnectAttempts++;
    console.log(`WebSocket 重连中... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

    setTimeout(() => {
      this.connect(options).catch((error) => {
        console.error('WebSocket 重连失败:', error);
      });
    }, this.reconnectDelay * this.reconnectAttempts);
  }

  /**
   * 断开连接
   */
  disconnect() {
    this.stopHeartbeat();
    if (this.isConnected) {
      uni.closeSocket({
        success: () => {
          this.isConnected = false;
          this.stompConnected = false;
        }
      });
    }
  }

  /**
   * 获取连接状态
   */
  getConnected() {
    return this.isConnected && this.stompConnected;
  }
}

// 导出单例
export default new WebSocketService();
