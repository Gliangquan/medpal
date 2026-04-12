<template>
  <view class="home-page">
    <view v-if="loadingUser" class="loading-state">
      <uni-load-more status="loading" />
    </view>

    <view v-else-if="!userInfo" class="loading-state">
      <text class="muted-text">未检测到登录信息，正在跳转登录页...</text>
    </view>

    <view v-else class="content-shell">
      <view class="hero-card">
        <text class="hero-role">{{ roleLabel }}</text>
        <text class="hero-name">{{ greetingText }}，{{ userInfo.userName || '用户' }}</text>
        <view class="hero-sub-row">
          <text class="hero-sub">今日重点：{{ focusText }}</text>
          <view class="notice-chip" @tap="goNotification">
            <image class="chip-icon" src="/static/icon_med/tixing.png" mode="aspectFit" />
            <text class="notice-count">{{ unreadCount }} 条未读</text>
          </view>
        </view>
      </view>

      <view v-if="isPatientRole">
        <view class="section-block">
          <view class="section-head">
            <text class="section-title">快捷入口</text>
          </view>
          <view class="action-grid">
            <view class="action-item" @tap="goAppointment">
              <image class="action-icon" src="/static/icon_med/bianji.png" mode="aspectFit" />
              <text class="action-title">发布陪诊单</text>
              <text class="action-sub">快速发单，1分钟完成</text>
            </view>
            <view class="action-item" @tap="goOrders">
              <image class="action-icon" src="/static/icon_med/dingdan.png" mode="aspectFit" />
              <text class="action-title">我的订单</text>
              <text class="action-sub">查看服务进度与状态</text>
            </view>
            <view class="action-item" @tap="goCompanionList">
              <image class="action-icon" src="/static/icon_med/yisheng.png" mode="aspectFit" />
              <text class="action-title">挑选陪诊员</text>
              <text class="action-sub">按评分与擅长筛选</text>
            </view>
            <view class="action-item" @tap="goMedicalRecord">
              <image class="action-icon" src="/static/icon_med/bingli.png" mode="aspectFit" />
              <text class="action-title">电子病历</text>
              <text class="action-sub">就诊记录统一管理</text>
            </view>
            <view class="action-item" @tap="goContentList">
              <image class="action-icon" src="/static/icon_med/suoming.png" mode="aspectFit" />
              <text class="action-title">健康科普</text>
              <text class="action-sub">查看就医指南与公告</text>
            </view>
            <view class="action-item" @tap="goNotification">
              <image class="action-icon" src="/static/icon_med/xiaoxi.png" mode="aspectFit" />
              <text class="action-title">消息中心</text>
              <text class="action-sub">订单与系统通知</text>
            </view>
            <view class="action-item" @tap="goEmergency">
              <image class="action-icon" src="/static/icon_med/tixing.png" mode="aspectFit" />
              <text class="action-title">紧急求助</text>
              <text class="action-sub">突发场景快速呼叫</text>
            </view>
          </view>
        </view>

        <view class="stats-row">
          <view class="stat-card">
            <text class="stat-number">{{ patientStats.processing }}</text>
            <text class="stat-label">进行中</text>
          </view>
          <view class="stat-card">
            <text class="stat-number">{{ patientStats.completed }}</text>
            <text class="stat-label">已完成</text>
          </view>
          <view class="stat-card">
            <text class="stat-number">{{ patientStats.cancelled }}</text>
            <text class="stat-label">已取消</text>
          </view>
        </view>

        <view class="section-block">
          <view class="section-head">
            <text class="section-title">我的进行中订单</text>
            <text class="section-link" @tap="goOrders">查看全部</text>
          </view>
          <view v-if="patientRecentOrders.length" class="list-wrap">
            <view v-for="item in patientRecentOrders" :key="item.id" class="order-card" @tap="goOrderDetail(item.id)">
              <view class="order-head">
                <text class="order-id">#{{ item.id }}</text>
                <text class="order-status" :style="{ color: item.statusColor }">{{ item.statusText }}</text>
              </view>
              <text class="order-line">{{ item.hospitalText }}</text>
              <text class="order-line">就诊时间：{{ item.appointmentText }}</text>
              <view class="order-foot">
                <text class="price-text">¥{{ item.totalPriceText }}</text>
                <text class="detail-link">订单详情</text>
              </view>
            </view>
          </view>
          <view v-else class="empty-box">
            <text class="muted-text">你还没有进行中的陪诊单</text>
            <button class="solid-btn" @tap="goAppointment">立即发布</button>
          </view>
        </view>

        <view class="section-block">
          <view class="section-head">
            <text class="section-title">推荐陪诊员</text>
            <text class="section-link" @tap="goCompanionList">更多</text>
          </view>
          <scroll-view v-if="companionRecommendations.length" scroll-x class="recommend-scroll">
            <view class="recommend-row">
              <view
                v-for="item in companionRecommendations"
                :key="item.id"
                class="companion-card"
                @tap="goCompanionDetail(item.id)"
              >
                <avatar :src="item.userAvatar" :name="item.userName || '陪诊员'" size="lg" radius="md" class="card-avatar" />
                <text class="companion-name">{{ item.userName || '陪诊员' }}</text>
                <text class="companion-meta">评分 {{ item.rating || '4.8' }}</text>
                <text class="companion-meta">服务 {{ item.serviceCount || 0 }} 次</text>
              </view>
            </view>
          </scroll-view>
          <view v-else class="empty-box">
            <text class="muted-text">暂无推荐，稍后再试</text>
          </view>
        </view>
      </view>

      <view v-else-if="isCompanionRole">
        <view class="section-block">
          <view class="section-head">
            <text class="section-title">工作台入口</text>
          </view>
          <view class="action-grid">
            <view class="action-item" @tap="refreshHome(true)">
              <image class="action-icon" src="/static/icon_med/shuaxing.png" mode="aspectFit" />
              <text class="action-title">刷新待接单</text>
              <text class="action-sub">同步最新可接订单</text>
            </view>
            <view class="action-item" @tap="goOrders">
              <image class="action-icon" src="/static/icon_med/dingdan.png" mode="aspectFit" />
              <text class="action-title">订单管理</text>
              <text class="action-sub">查看全部服务订单</text>
            </view>
            <view class="action-item" @tap="goNotification">
              <image class="action-icon" src="/static/icon_med/xiaoxi.png" mode="aspectFit" />
              <text class="action-title">消息通知</text>
              <text class="action-sub">掌握用户与系统动态</text>
            </view>
            <view class="action-item" @tap="goContentList">
              <image class="action-icon" src="/static/icon_med/suoming.png" mode="aspectFit" />
              <text class="action-title">平台公告</text>
              <text class="action-sub">查看制度与健康科普</text>
            </view>
            <view class="action-item" @tap="goProfile">
              <image class="action-icon" src="/static/icon_med/wode.png" mode="aspectFit" />
              <text class="action-title">我的资料</text>
              <text class="action-sub">维护认证与服务信息</text>
            </view>
            <view class="action-item" @tap="goCompanionCertification">
              <image class="action-icon" src="/static/icon_med/jiangbei.png" mode="aspectFit" />
              <text class="action-title">资质认证</text>
              <text class="action-sub">{{ companionCertificationHint }}</text>
            </view>
            <view class="action-item" @tap="goChat">
              <image class="action-icon" src="/static/icon_med/zixun.png" mode="aspectFit" />
              <text class="action-title">在线沟通</text>
              <text class="action-sub">快速响应患者咨询</text>
            </view>
            <view class="action-item" @tap="goEmergency">
              <image class="action-icon" src="/static/icon_med/tixing.png" mode="aspectFit" />
              <text class="action-title">紧急联动</text>
              <text class="action-sub">异常场景快速上报</text>
            </view>
          </view>
        </view>

        <view class="stats-row companion-mode">
          <view class="stat-card">
            <text class="stat-number">{{ companionStats.market }}</text>
            <text class="stat-label">待接单池</text>
          </view>
          <view class="stat-card">
            <text class="stat-number">{{ companionStats.confirmed }}</text>
            <text class="stat-label">已接单</text>
          </view>
          <view class="stat-card">
            <text class="stat-number">{{ companionStats.completed }}</text>
            <text class="stat-label">已完成</text>
          </view>
        </view>

        <view class="section-block">
          <view class="section-head">
            <text class="section-title">待接单大厅</text>
            <text class="section-link" @tap="refreshHome(true)">刷新</text>
          </view>
          <view v-if="companionMarketOrders.length" class="list-wrap">
            <view v-for="item in companionMarketOrders" :key="item.id" class="order-card">
              <view class="order-head">
                <text class="order-id">#{{ item.id }}</text>
                <text class="order-status waiting-color">待接单</text>
              </view>
              <text class="order-line">{{ item.hospitalText }}</text>
              <text class="order-line">就诊时间：{{ item.appointmentText }}</text>
              <view class="order-foot">
                <text class="price-text">¥{{ item.totalPriceText }}</text>
                <button size="mini" class="grab-btn" @tap.stop="acceptOrder(item)">
                  <image class="btn-icon" src="/static/icon_med/wancheng.png" mode="aspectFit" />
                  <text>立即接单</text>
                </button>
              </view>
            </view>
          </view>
          <view v-else class="empty-box">
            <text class="muted-text">当前没有待接单，建议稍后刷新</text>
          </view>
        </view>

        <view class="section-block">
          <view class="section-head">
            <text class="section-title">我的进行中服务</text>
            <text class="section-link" @tap="goOrders">订单页</text>
          </view>
          <view v-if="companionMyOrders.length" class="list-wrap">
            <view v-for="item in companionMyOrders" :key="item.id" class="order-card" @tap="goOrderDetail(item.id)">
              <view class="order-head">
                <text class="order-id">#{{ item.id }}</text>
                <text class="order-status" :style="{ color: item.statusColor }">{{ item.statusText }}</text>
              </view>
              <text class="order-line">{{ item.hospitalText }}</text>
              <text class="order-line">就诊时间：{{ item.appointmentText }}</text>
              <view class="order-foot">
                <text class="price-text">¥{{ item.totalPriceText }}</text>
                <text class="detail-link">查看详情</text>
              </view>
            </view>
          </view>
          <view v-else class="empty-box">
            <text class="muted-text">你还没有进行中的服务订单</text>
          </view>
        </view>
      </view>

      <view v-else class="section-block">
        <view class="section-head">
          <text class="section-title">账户信息</text>
        </view>
        <view class="empty-box">
          <text class="muted-text">当前角色：{{ userInfo.userRole || 'unknown' }}</text>
          <button class="solid-btn" @tap="refreshHome(true)">重新加载</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { companionApi, notificationApi, orderApi, userApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';
import {
  certStatusText,
  isCompanionCertified,
  normalizeRole
} from '@/utils/permission.js';
import avatar from '@/components/avatar.vue';

const STATUS_TEXT = {
  pending: '待接单',
  confirmed: '已接单',
  serving: '已接单',
  completion_pending: '待确认完成',
  completed: '已完成',
  cancelled: '已取消'
};

const STATUS_COLOR = {
  pending: '#e07a00',
  confirmed: '#2053d8',
  serving: '#2053d8',
  completion_pending: '#6f52ed',
  completed: '#63708a',
  cancelled: '#d94848'
};

export default {
  components: {
    avatar
  },
  data() {
    return {
      loadingUser: true,
      userInfo: null,
      unreadCount: 0,
      patientStats: {
        processing: 0,
        completed: 0,
        cancelled: 0
      },
      companionStats: {
        market: 0,
        confirmed: 0,
        completed: 0
      },
      patientRecentOrders: [],
      companionRecommendations: [],
      companionMarketOrders: [],
      companionMyOrders: []
    };
  },
  computed: {
    normalizedRole() {
      return normalizeRole(this.userInfo);
    },
    isPatientRole() {
      return this.normalizedRole === 'patient';
    },
    isCompanionRole() {
      return this.normalizedRole === 'companion';
    },
    roleLabel() {
      if (this.isPatientRole) return '普通患者端';
      if (this.isCompanionRole) return '陪诊员工作台';
      if (this.normalizedRole === 'admin') return '管理员';
      return '访客';
    },
    greetingText() {
      const hour = new Date().getHours();
      if (hour < 12) return '上午好';
      if (hour < 18) return '下午好';
      return '晚上好';
    },
    focusText() {
      if (this.isPatientRole) return '发布清晰需求，等待优先响应';
      if (this.isCompanionRole) return '关注待接单池，优先接近期开诊订单';
      return '保持账号信息完整';
    },
    companionCertificationHint() {
      if (!this.isCompanionRole) return '';
      if (isCompanionCertified(this.userInfo)) return '已认证，可正常接单';
      const real = certStatusText(this.userInfo?.realNameStatus);
      const qualification = certStatusText(this.userInfo?.qualificationStatus);
      return `实名${real} / 资质${qualification}`;
    }
  },
  onLoad() {
    this.initHome();
  },
  onShow() {
    if (this.userInfo?.id) {
      this.refreshHome();
    }
  },
  onPullDownRefresh() {
    this.refreshHome(true).finally(() => {
      uni.stopPullDownRefresh();
    });
  },
  methods: {
    getOrderStatusText(order) {
      const status = order?.orderStatus;
      const paymentStatus = order?.paymentStatus;
      const hasCompanion = Boolean(order?.companionId);
      if (status === 'pending') {
        if (paymentStatus !== 'paid') return '待支付';
        return hasCompanion ? '已支付待确认' : '已支付待接单';
      }
      if (status === 'confirmed' && !hasCompanion) {
        return paymentStatus === 'paid' ? '已支付待接单' : '待支付';
      }
      if (status === 'completion_pending') {
        return this.isPatientRole ? '待确认完成' : '待用户确认';
      }
      return STATUS_TEXT[status] || '未知';
    },
    getOrderStatusColor(order) {
      const status = order?.orderStatus;
      const paymentStatus = order?.paymentStatus;
      if (status === 'pending' && paymentStatus !== 'paid') return '#f08c00';
      if (status === 'pending') return '#2053d8';
      return STATUS_COLOR[status] || '#63708a';
    },
    async initHome() {
      await this.loadUser();
      await this.refreshHome();
      this.loadingUser = false;
    },
    async loadUser() {
      const stored = uni.getStorageSync('userInfo');
      if (!stored?.id) {
        this.userInfo = null;
        uni.reLaunch({ url: '/pages/login/index' });
        return;
      }
      this.userInfo = stored;
      try {
        const freshUser = await userApi.fetchCurrentUser();
        if (freshUser?.id) {
          this.userInfo = freshUser;
          uni.setStorageSync('userInfo', freshUser);
        }
      } catch (error) {
        console.warn('刷新用户信息失败，使用本地缓存', error);
      }
    },
    async refreshHome(showToast = false) {
      if (!this.userInfo?.id) return;
      try {
        await this.loadUnreadCount();
        if (this.isPatientRole) {
          await Promise.all([this.loadPatientOrders(), this.loadCompanionRecommendations()]);
        } else if (this.isCompanionRole) {
          await this.loadCompanionOrders();
        }
        if (showToast) {
          uni.showToast({ title: '已刷新', icon: 'success' });
        }
      } catch (error) {
        console.error('首页刷新失败', error);
        if (showToast) {
          uni.showToast({ title: '刷新失败', icon: 'none' });
        }
      }
    },
    async loadUnreadCount() {
      try {
        const count = await notificationApi.unreadCount(this.userInfo.id);
        this.unreadCount = Number(count || 0);
      } catch (error) {
        this.unreadCount = 0;
      }
    },
    async loadPatientOrders() {
      const page = await orderApi.list({
        current: 1,
        size: 20,
        userId: this.userInfo.id
      });
      const list = this.normalizeOrders(page.records || []);
      const processing = list.filter((item) => ['pending', 'confirmed', 'completion_pending'].includes(item.orderStatus));
      this.patientRecentOrders = processing.slice(0, 3);
      this.patientStats = {
        processing: processing.length,
        completed: list.filter((item) => item.orderStatus === 'completed').length,
        cancelled: list.filter((item) => item.orderStatus === 'cancelled').length
      };
    },
    async loadCompanionRecommendations() {
      try {
        const page = await companionApi.list({ current: 1, size: 8 });
        this.companionRecommendations = (page.records || []).slice(0, 6);
      } catch (error) {
        this.companionRecommendations = [];
      }
    },
    async loadCompanionOrders() {
      const [pendingMarketPage, confirmedMarketPage, myPage] = await Promise.all([
        orderApi.list({
          current: 1,
          size: 20,
          orderStatus: 'pending'
        }),
        orderApi.list({
          current: 1,
          size: 20,
          orderStatus: 'confirmed'
        }),
        orderApi.list({
          current: 1,
          size: 20,
          companionId: this.userInfo.id
        })
      ]);

      const marketRaw = [...(pendingMarketPage.records || []), ...(confirmedMarketPage.records || [])];
      const marketUniqueMap = new Map(marketRaw.map((item) => [String(item.id), item]));
      const marketList = this.normalizeOrders(
        Array.from(marketUniqueMap.values()).filter((item) => !item.companionId && item.paymentStatus === 'paid')
      );
      const myList = this.normalizeOrders(myPage.records || []);
      this.companionMarketOrders = marketList.slice(0, 3);
      this.companionMyOrders = myList.filter((item) => ['confirmed', 'serving', 'completion_pending'].includes(item.orderStatus)).slice(0, 3);
      this.companionStats = {
        market: Number(marketList.length || 0),
        confirmed: myList.filter((item) => item.orderStatus === 'confirmed').length,
        completed: myList.filter((item) => item.orderStatus === 'completed').length
      };
    },
    normalizeOrders(list) {
      return list.map((item) => ({
        ...item,
        statusText: this.getOrderStatusText(item),
        statusColor: this.getOrderStatusColor(item),
        appointmentText: formatDateTime(item.appointmentDate || item.createTime) || '时间待确认',
        hospitalText: item.hospitalName || (item.hospitalId ? `医院ID ${item.hospitalId}` : '医院待确认'),
        totalPriceText: item.totalPrice ?? 0
      }));
    },
    async ensureCompanionCertifiedForAction() {
      if (!this.isCompanionRole) return false;
      let currentUser = this.userInfo;
      try {
        const fresh = await userApi.fetchCurrentUser();
        if (fresh?.id) {
          currentUser = fresh;
          this.userInfo = fresh;
          uni.setStorageSync('userInfo', fresh);
        }
      } catch (error) {
        // ignore, fallback to local cache
      }
      if (isCompanionCertified(currentUser)) return true;
      uni.showModal({
        title: '请先完成认证',
        content: '完成实名认证与资质认证后才可接单，是否前往认证中心？',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: '/pages/companion/certification' });
          }
        }
      });
      return false;
    },
    async acceptOrder(item) {
      if (item?.paymentStatus !== 'paid') {
        uni.showToast({ title: '订单未支付，暂不可接单', icon: 'none' });
        return;
      }
      const isPending = item?.orderStatus === 'pending';
      const isLegacyUnassignedConfirmed = item?.orderStatus === 'confirmed' && !item?.companionId;
      if (!isPending && !isLegacyUnassignedConfirmed) {
        uni.showToast({ title: '当前状态不可接单', icon: 'none' });
        return;
      }
      const certified = await this.ensureCompanionCertifiedForAction();
      if (!certified) return;
      uni.showModal({
        title: '确认接单',
        content: `确定接订单 #${item.id} 吗？`,
        success: async (res) => {
          if (!res.confirm) return;
          try {
            await orderApi.accept(item.id, this.userInfo.id);
            uni.showToast({ title: '接单成功', icon: 'success' });
            this.refreshHome();
          } catch (error) {
            uni.showToast({ title: error.message || '接单失败', icon: 'none' });
          }
        }
      });
    },
    goOrderDetail(id) {
      uni.navigateTo({ url: `/pages/order/detail?id=${id}` });
    },
    goAppointment() {
      if (!this.isPatientRole) {
        uni.showToast({ title: '陪诊员端不支持发单预约', icon: 'none' });
        return;
      }
      uni.navigateTo({ url: '/pages/appointment/index' });
    },
    goOrders() {
      uni.switchTab({ url: '/pages/order/index' });
    },
    goCompanionList() {
      uni.navigateTo({ url: '/pages/companion/list' });
    },
    goCompanionDetail(id) {
      uni.navigateTo({ url: `/pages/companion/detail?id=${id}` });
    },
    goMedicalRecord() {
      if (!this.isPatientRole) {
        uni.showToast({ title: '患者端可查看病历', icon: 'none' });
        return;
      }
      uni.navigateTo({ url: '/pages/medical-record/index' });
    },
    goContentList() {
      uni.navigateTo({ url: '/pages/content/list' });
    },
    goProfile() {
      uni.switchTab({ url: '/pages/profile/index' });
    },
    goChat() {
      uni.navigateTo({ url: '/pages/chat/index' });
    },
    goCompanionCertification() {
      if (!this.isCompanionRole) return;
      uni.navigateTo({ url: '/pages/companion/certification' });
    },
    goEmergency() {
      uni.navigateTo({ url: '/pages/emergency/index' });
    },
    goNotification() {
      uni.switchTab({ url: '/pages/notification/index' });
    }
  }
};
</script>

<style scoped lang="scss">
.home-page {
  --bg-start: #edf3ff;
  --bg-end: #fbfcff;
  --ink-main: #182238;
  --ink-sub: #5e6a82;
  --brand: #2b5fe3;
  --brand-weak: #ecf2ff;
  --brand-strong: #123caa;
  min-height: 100vh;
  background: linear-gradient(160deg, var(--bg-start), var(--bg-end) 60%);
}

.loading-state {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.content-shell {
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.hero-card {
  border-radius: 28rpx;
  padding: 28rpx;
  background: linear-gradient(140deg, #1f4ccf, #5f8dff 72%);
  color: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(30, 78, 199, 0.24);
}

.hero-role {
  font-size: 22rpx;
  letter-spacing: 2rpx;
  opacity: 0.86;
}

.hero-name {
  display: block;
  margin-top: 10rpx;
  font-size: 40rpx;
  font-weight: 700;
}

.hero-sub-row {
  margin-top: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.hero-sub {
  font-size: 22rpx;
  opacity: 0.92;
}

.notice-chip {
  display: flex;
  align-items: center;
  gap: 8rpx;
  border-radius: 999rpx;
  padding: 8rpx 16rpx;
  background: #ffffff;
}

.chip-icon {
  width: 26rpx;
  height: 26rpx;
}

.notice-count {
  color: #1f4ccf;
  font-size: 22rpx;
  font-weight: 600;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.action-item {
  border-radius: 24rpx;
  padding: 24rpx 22rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 26rpx rgba(21, 45, 102, 0.08);
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.action-icon {
  width: 42rpx;
  height: 42rpx;
}

.action-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: var(--ink-main);
}

.action-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: var(--ink-sub);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14rpx;
}

.companion-mode {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.stat-card {
  background: var(--brand-weak);
  border-radius: 20rpx;
  text-align: center;
  padding: 18rpx 6rpx;
}

.stat-number {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: var(--brand-strong);
  font-family: 'DIN Alternate', 'PingFang SC', 'Helvetica Neue', sans-serif;
}

.stat-label {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
  color: var(--ink-sub);
}

.section-block {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 22rpx;
  box-shadow: 0 8rpx 26rpx rgba(21, 45, 102, 0.06);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 700;
  color: var(--ink-main);
}

.section-link {
  font-size: 22rpx;
  color: var(--brand);
}

.list-wrap {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.order-card {
  border-radius: 18rpx;
  background: #f7f9ff;
  border: 1rpx solid #e5ebfb;
  padding: 18rpx;
}

.order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-id {
  font-size: 22rpx;
  color: #72809d;
  font-weight: 600;
}

.order-status {
  font-size: 22rpx;
  font-weight: 700;
}

.waiting-color {
  color: #d98200;
}

.order-line {
  display: block;
  margin-top: 8rpx;
  font-size: 23rpx;
  color: #2d3950;
}

.order-foot {
  margin-top: 14rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price-text {
  font-size: 30rpx;
  color: #1948be;
  font-weight: 800;
  font-family: 'DIN Alternate', 'PingFang SC', 'Helvetica Neue', sans-serif;
}

.detail-link {
  font-size: 22rpx;
  color: var(--brand);
}

.empty-box {
  padding: 24rpx 0;
  text-align: center;
}

.muted-text {
  font-size: 23rpx;
  color: #7d8aa3;
}

.solid-btn {
  margin-top: 18rpx;
  background: var(--brand);
  color: #ffffff;
  border-radius: 999rpx;
  width: 260rpx;
}

.recommend-scroll {
  white-space: nowrap;
}

.recommend-row {
  display: inline-flex;
  gap: 14rpx;
  padding-bottom: 4rpx;
}

.companion-card {
  width: 250rpx;
  border-radius: 18rpx;
  background: #f7f9ff;
  border: 1rpx solid #e5ebfb;
  padding: 18rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.card-avatar {
  margin-bottom: 12rpx;
}

.companion-name {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: #1f2d4a;
}

.companion-meta {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #66748f;
}

.grab-btn {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  background: #214fd4;
  color: #ffffff;
  border-radius: 999rpx;
  font-size: 20rpx;
  line-height: 1.6;
  padding: 0 20rpx;
}

.btn-icon {
  width: 24rpx;
  height: 24rpx;
}
</style>
