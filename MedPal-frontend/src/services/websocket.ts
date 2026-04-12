import { Client, Message } from 'stompjs';
import SockJS from 'sockjs-client';

export interface WebSocketMessage {
  type: 'chat' | 'read' | 'typing' | 'stopTyping' | 'recall' | 'error';
  message?: any;
  sender?: any;
  messageId?: number;
  senderId?: number;
  isTyping?: boolean;
  content?: string;
  readTime?: string;
}

export interface WebSocketConfig {
  url: string;
  token: string;
  userId: number;
  userType: 'user' | 'companion';
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: any) => void;
  onMessage?: (message: WebSocketMessage) => void;
}

class WebSocketService {
  private client: Client | null = null;
  private config: WebSocketConfig | null = null;
  private messageHandlers: Map<string, (msg: WebSocketMessage) => void> = new Map();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;
  private messageQueue: any[] = [];
  private isConnected = false;

  /**
   * 初始化 WebSocket 连接
   */
  connect(config: WebSocketConfig): Promise<void> {
    return new Promise((resolve, reject) => {
      this.config = config;

      // 创建 SockJS 连接
      const socket = new SockJS(config.url);

      // 创建 STOMP 客户端
      this.client = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: this.reconnectDelay,
        heartbeatIncoming: 30000,
        heartbeatOutgoing: 30000,
        onConnect: () => {
          this.isConnected = true;
          this.reconnectAttempts = 0;

          // 订阅消息队列
          this.subscribe(`/user/${config.userId}/queue/messages`, (frame: Message) => {
            const message = JSON.parse(frame.body) as WebSocketMessage;
            this.handleMessage(message);
          });

          config.onConnect?.();
          resolve();
        },
        onStompError: (frame) => {
          this.isConnected = false;
          const error = new Error(`STOMP error: ${frame.body}`);
          config.onError?.(error);
          reject(error);
        },
        onWebSocketError: (error) => {
          this.isConnected = false;
          config.onError?.(error);
          reject(error);
        },
        onWebSocketClose: () => {
          this.isConnected = false;
          config.onDisconnect?.();
          this.attemptReconnect();
        },
      });

      // 连接头中添加 Authorization
      const headers: any = {
        'Authorization': `Bearer ${config.token}`,
      };

      this.client.connect(headers, () => {
        // 连接成功
      });
    });
  }

  /**
   * 订阅消息
   */
  private subscribe(destination: string, callback: (frame: Message) => void): void {
    if (!this.client) return;

    this.client.subscribe(destination, callback);
  }

  /**
   * 发送聊天消息
   */
  sendMessage(receiverId: number, content: string, messageType: 'text' | 'image' = 'text', imageUrl?: string): void {
    if (!this.client || !this.isConnected) {
      this.messageQueue.push({ receiverId, content, messageType, imageUrl });
      return;
    }

    const message = {
      orderId: this.getOrderId(),
      content,
      messageType,
      imageUrl: imageUrl || null,
    };

    this.client.send(`/app/chat.send/${receiverId}`, {}, JSON.stringify(message));
  }

  /**
   * 标记消息已读
   */
  markAsRead(messageId: number): void {
    if (!this.client || !this.isConnected) return;

    this.client.send(`/app/chat.read/${messageId}`, {}, '');
  }

  /**
   * 发送输入状态
   */
  sendTyping(receiverId: number): void {
    if (!this.client || !this.isConnected) return;

    this.client.send(`/app/chat.typing/${receiverId}`, {}, '');
  }

  /**
   * 停止输入状态
   */
  stopTyping(receiverId: number): void {
    if (!this.client || !this.isConnected) return;

    this.client.send(`/app/chat.stopTyping/${receiverId}`, {}, '');
  }

  /**
   * 撤回消息
   */
  recallMessage(messageId: number): void {
    if (!this.client || !this.isConnected) return;

    this.client.send(`/app/chat.recall/${messageId}`, {}, '');
  }

  /**
   * 处理接收到的消息
   */
  private handleMessage(message: WebSocketMessage): void {
    // 调用全局消息处理器
    this.config?.onMessage?.(message);

    // 调用特定类型的消息处理器
    const handler = this.messageHandlers.get(message.type);
    if (handler) {
      handler(message);
    }
  }

  /**
   * 注册消息处理器
   */
  on(type: string, handler: (msg: WebSocketMessage) => void): void {
    this.messageHandlers.set(type, handler);
  }

  /**
   * 移除消息处理器
   */
  off(type: string): void {
    this.messageHandlers.delete(type);
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    if (this.client) {
      this.client.disconnect(() => {
        this.isConnected = false;
        this.config?.onDisconnect?.();
      });
    }
  }

  /**
   * 尝试重新连接
   */
  private attemptReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('WebSocket 重连失败，已达到最大重试次数');
      return;
    }

    this.reconnectAttempts++;
    console.log(`WebSocket 重连中... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

    setTimeout(() => {
      if (this.config) {
        this.connect(this.config).catch((error) => {
          console.error('WebSocket 重连失败:', error);
        });
      }
    }, this.reconnectDelay * this.reconnectAttempts);
  }

  /**
   * 获取连接状态
   */
  getConnected(): boolean {
    return this.isConnected;
  }

  /**
   * 获取订单 ID（从 localStorage 获取）
   */
  private getOrderId(): number {
    const orderInfo = localStorage.getItem('currentOrder');
    if (orderInfo) {
      try {
        const order = JSON.parse(orderInfo);
        return order.id || 0;
      } catch {
        return 0;
      }
    }
    return 0;
  }

  /**
   * 处理消息队列（连接成功后发送）
   */
  private flushMessageQueue(): void {
    while (this.messageQueue.length > 0) {
      const msg = this.messageQueue.shift();
      this.sendMessage(msg.receiverId, msg.content, msg.messageType, msg.imageUrl);
    }
  }
}

export default new WebSocketService();
