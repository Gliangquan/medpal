<template>
  <view class="page-content">
    <uni-card :border="false" padding="24" @tap="goEdit">
      <view class="flex items-center gap-md">
        <view class="avatar">
          <text v-if="!avatarUrl">{{ user.userName ? user.userName.slice(0, 1) : '我' }}</text>
          <image v-else :src="avatarUrl" mode="aspectFill" />
        </view>
        <view style="flex: 1;">
          <text class="text-base font-semibold text-primary">{{ user.userName || '未设置昵称' }}</text>
          <text class="text-sm text-muted" style="display: block; margin-top: 4rpx;">{{ user.userAccount || '未登录' }}</text>
          <uni-tag :text="roleTagText" size="small" style="margin-top: 8rpx;" />
        </view>
        <image class="header-arrow" src="/static/icon_med/yulan.png" mode="aspectFit" />
      </view>
    </uni-card>

    <uni-section title="常用功能" class="section"></uni-section>
    <uni-list :border="false">
      <uni-list-item :title="isCompanionRole ? '接单中心' : '我的订单'" thumb="/static/icon_med/dingdan.png" showArrow clickable @click="goOrders"></uni-list-item>
      <uni-list-item v-if="isPatientRole" title="我的病历" thumb="/static/icon_med/bingli.png" showArrow clickable @click="goRecords"></uni-list-item>
      <uni-list-item v-if="isCompanionRole" title="陪诊认证" thumb="/static/icon_med/jiangbei.png" :right-text="certificationStatusText" showArrow clickable @click="goCertification"></uni-list-item>
      <uni-list-item title="消息通知" thumb="/static/icon_med/xiaoxi.png" showArrow clickable @click="goNotification">
        <template v-slot:footer>
          <uni-badge v-if="unreadCount > 0" :text="badgeText" type="error"></uni-badge>
        </template>
      </uni-list-item>
      <uni-list-item title="紧急求助" thumb="/static/icon_med/tixing.png" showArrow clickable @click="goEmergency"></uni-list-item>
    </uni-list>

    <uni-section title="账号设置" class="section"></uni-section>
    <uni-list :border="false">
      <uni-list-item title="编辑资料" thumb="/static/icon_med/bianji.png" showArrow clickable @click="goEdit"></uni-list-item>
      <uni-list-item title="通知与隐私设置" thumb="/static/icon_med/shezhi.png" showArrow clickable @click="goSettings"></uni-list-item>
    </uni-list>

    <view class="section logout-wrap">
      <text class="text-sm font-semibold logout-title">账号操作</text>
      <button type="warn" @tap="logout" style="border-radius: 999rpx;">退出登录</button>
    </view>

    <view class="version-wrap">
      <text class="text-sm text-muted">版本 1.0.0 · 最后更新 2026-02-21</text>
    </view>
  </view>
</template>

<script>
import { notificationApi, userApi } from '@/utils/api.js';
import { normalizeFileUrl } from '@/utils/format.js';
import {
  certStatusText,
  isCompanionRole as checkCompanionRole,
  isPatientRole as checkPatientRole,
  normalizeRole
} from '@/utils/permission.js';

export default {
  data() {
    return {
      user: {},
      unreadCount: 0
    };
  },
  computed: {
    badgeText() {
      return this.unreadCount > 99 ? '99+' : String(this.unreadCount);
    },
    isCompanionRole() {
      return checkCompanionRole(this.user);
    },
    isPatientRole() {
      return checkPatientRole(this.user);
    },
    roleTagText() {
      const role = normalizeRole(this.user);
      if (role === 'companion') return '陪诊员';
      if (role === 'admin') return '管理员';
      if (role === 'patient') return '患者';
      return '用户';
    },
    certificationStatusText() {
      if (!this.isCompanionRole) return '';
      const real = certStatusText(this.user?.realNameStatus);
      const qualification = certStatusText(this.user?.qualificationStatus);
      return `实名${real} / 资质${qualification}`;
    },
    avatarUrl() {
      return normalizeFileUrl(this.user?.userAvatar);
    }
  },
  async onShow() {
    await this.loadUser();
    await this.loadUnreadCount();
  },
  methods: {
    async loadUser() {
      try {
        const user = await userApi.fetchCurrentUser();
        this.user = user || {};
        if (user) {
          uni.setStorageSync('userInfo', user);
        }
      } catch (error) {
        this.user = uni.getStorageSync('userInfo') || {};
      }
    },
    async loadUnreadCount() {
      const user = this.user || uni.getStorageSync('userInfo');
      if (!user?.id) {
        this.unreadCount = 0;
        return;
      }
      try {
        const count = await notificationApi.unreadCount(user.id);
        this.unreadCount = Number(count) || 0;
      } catch (error) {
        this.unreadCount = 0;
      }
    },
    goEdit() {
      uni.navigateTo({ url: '/pages/edit-profile/index' });
    },
    goOrders() {
      uni.switchTab({ url: '/pages/order/index' });
    },
    goRecords() {
      if (!this.isPatientRole) {
        uni.showToast({ title: '仅患者可查看病历', icon: 'none' });
        return;
      }
      uni.navigateTo({ url: '/pages/medical-record/index' });
    },
    goNotification() {
      uni.switchTab({ url: '/pages/notification/index' });
    },
    goEmergency() {
      uni.navigateTo({ url: '/pages/emergency/index' });
    },
    goCertification() {
      if (!this.isCompanionRole) return;
      uni.navigateTo({ url: '/pages/companion/certification' });
    },
    goSettings() {
      uni.navigateTo({ url: '/pages/settings/index' });
    },
    async logout() {
      const confirmed = await new Promise((resolve) => {
        uni.showModal({
          title: '退出登录',
          content: '确定要退出登录吗？',
          success: (res) => resolve(res.confirm)
        });
      });
      if (!confirmed) return;
      try {
        await userApi.logout();
      } catch (error) {
        // ignore
      }
      uni.clearStorageSync();
      uni.reLaunch({ url: '/pages/login/index' });
    }
  }
};
</script>

<style lang="scss">
@import "@/styles/common.scss";

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: $primary-color;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  overflow: hidden;

  image {
    width: 100%;
    height: 100%;
  }
}

.header-arrow {
  width: 32rpx;
  height: 32rpx;
}

.logout-wrap {
  background: rgba(255, 92, 92, 0.08);
  border-radius: 18rpx;
  padding: 24rpx;
}

.logout-title {
  color: #ff5c5c;
  display: block;
  margin-bottom: 16rpx;
}

.version-wrap {
  text-align: center;
  margin-top: 32rpx;
  margin-bottom: 32rpx;
}
</style>
