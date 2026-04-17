<template>
  <view class="page">
    <uni-section title="通知设置" type="line"></uni-section>
    <view class="card">
      <view class="setting-row">
        <view>
          <text class="label">订单通知</text>
          <text class="desc">订单状态变更提醒</text>
        </view>
        <switch :checked="notificationSettings.orderNotificationEnabled" color="#2f65f9" @change="updateSetting('orderNotificationEnabled', $event)" />
      </view>
      <view class="setting-row">
        <view>
          <text class="label">活动通知</text>
          <text class="desc">平台活动和优惠信息</text>
        </view>
        <switch :checked="notificationSettings.activityNotificationEnabled" color="#2f65f9" @change="updateSetting('activityNotificationEnabled', $event)" />
      </view>
      <view class="setting-row">
        <view>
          <text class="label">健康科普推送</text>
          <text class="desc">健康知识和就医指南</text>
        </view>
        <switch :checked="notificationSettings.healthPushEnabled" color="#2f65f9" @change="updateSetting('healthPushEnabled', $event)" />
      </view>
      <view class="setting-row" style="margin-bottom: 0;">
        <view>
          <text class="label">聊天通知</text>
          <text class="desc">患者与陪诊员消息提醒</text>
        </view>
        <switch :checked="notificationSettings.chatNotificationEnabled" color="#2f65f9" @change="updateSetting('chatNotificationEnabled', $event)" />
      </view>
    </view>

    <uni-section title="隐私设置" type="line"></uni-section>
    <view class="card">
      <view v-if="isPatientRole" class="setting-row">
        <view>
          <text class="label">病历信息可见性</text>
          <text class="desc">是否允许已接单陪诊员查看病历</text>
        </view>
        <switch :checked="privacySettings.medicalRecordVisible" color="#2f65f9" @change="updatePrivacy('medicalRecordVisible', $event)" />
      </view>
      <view class="setting-row">
        <view>
          <text class="label">个人资料可见性</text>
          <text class="desc">是否公开个人资料</text>
        </view>
        <switch :checked="privacySettings.profileVisible" color="#2f65f9" @change="updatePrivacy('profileVisible', $event)" />
      </view>
      <view class="setting-row" style="margin-bottom: 0;">
        <view>
          <text class="label">聊天记录保存时长</text>
          <text class="desc">{{ privacySettings.chatHistorySaveDays }} 天</text>
        </view>
        <uni-number-box :min="7" :max="180" :value="privacySettings.chatHistorySaveDays" @change="updateChatDays" />
      </view>
    </view>

    <uni-section title="账户安全" type="line"></uni-section>
    <view class="card">
      <view class="password-form">
        <uni-easyinput v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" />
        <uni-easyinput v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码（至少6位）" />
        <uni-easyinput v-model="passwordForm.checkPassword" type="password" placeholder="请再次输入新密码" />
        <button class="btn-primary-inline" @tap="submitPasswordChange">修改密码</button>
      </view>
    </view>

    <button class="btn-ghost" @tap="clearCache">清除本地缓存</button>
  </view>
</template>

<script>
import { settingsApi, userApi } from '@/utils/api.js';
import { isPatientRole as checkPatientRole } from '@/utils/permission.js';

export default {
  data() {
    return {
      currentUser: null,
      notificationSettings: {
        orderNotificationEnabled: true,
        activityNotificationEnabled: true,
        healthPushEnabled: true,
        chatNotificationEnabled: true
      },
      privacySettings: {
        medicalRecordVisible: false,
        profileVisible: true,
        chatHistorySaveDays: 30
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        checkPassword: ''
      }
    };
  },
  computed: {
    isPatientRole() {
      return checkPatientRole(this.currentUser);
    }
  },
  async onLoad() {
    await this.loadSettings();
  },
  methods: {
    async loadSettings() {
      const user = uni.getStorageSync('userInfo');
      if (!user?.id) return;
      this.currentUser = user;
      try {
        const result = await settingsApi.get(user.id);
        if (!result) return;
        this.notificationSettings = {
          orderNotificationEnabled: !!result.orderNotificationEnabled,
          activityNotificationEnabled: !!result.activityNotificationEnabled,
          healthPushEnabled: !!result.healthPushEnabled,
          chatNotificationEnabled: !!result.chatNotificationEnabled
        };
        this.privacySettings = {
          medicalRecordVisible: !!result.medicalRecordVisible,
          profileVisible: !!result.profileVisible,
          chatHistorySaveDays: result.chatHistorySaveDays || 30
        };
      } catch (error) {
        uni.showToast({ title: '加载设置失败', icon: 'none' });
      }
    },
    async updateSetting(key, event) {
      this.notificationSettings = {
        ...this.notificationSettings,
        [key]: event.detail.value
      };
      const user = uni.getStorageSync('userInfo');
      if (!user?.id) return;
      try {
        await settingsApi.updateNotification(user.id, {
          orderNotificationEnabled: this.notificationSettings.orderNotificationEnabled ? 1 : 0,
          activityNotificationEnabled: this.notificationSettings.activityNotificationEnabled ? 1 : 0,
          healthPushEnabled: this.notificationSettings.healthPushEnabled ? 1 : 0,
          chatNotificationEnabled: this.notificationSettings.chatNotificationEnabled ? 1 : 0
        });
        uni.showToast({ title: '保存成功', icon: 'success' });
      } catch (error) {
        uni.showToast({ title: '保存失败', icon: 'none' });
      }
    },
    async updatePrivacy(key, event) {
      this.privacySettings = {
        ...this.privacySettings,
        [key]: event.detail.value
      };
      const user = uni.getStorageSync('userInfo');
      if (!user?.id) return;
      try {
        await settingsApi.updatePrivacy(user.id, { ...this.privacySettings });
        uni.showToast({ title: '保存成功', icon: 'success' });
      } catch (error) {
        uni.showToast({ title: '保存失败', icon: 'none' });
      }
    },
    async updateChatDays(value) {
      this.privacySettings.chatHistorySaveDays = value;
      await this.updatePrivacy('chatHistorySaveDays', { detail: { value } });
    },
    async submitPasswordChange() {
      const { oldPassword, newPassword, checkPassword } = this.passwordForm;
      if (!oldPassword || !newPassword || !checkPassword) {
        uni.showToast({ title: '请完整填写密码信息', icon: 'none' });
        return;
      }
      if (newPassword.length < 6) {
        uni.showToast({ title: '新密码至少 6 位', icon: 'none' });
        return;
      }
      if (newPassword !== checkPassword) {
        uni.showToast({ title: '两次输入的新密码不一致', icon: 'none' });
        return;
      }
      try {
        await userApi.changePassword({ oldPassword, newPassword, checkPassword });
        this.passwordForm = {
          oldPassword: '',
          newPassword: '',
          checkPassword: ''
        };
        uni.showToast({ title: '密码修改成功', icon: 'success' });
      } catch (error) {
        uni.showToast({ title: error.message || '修改密码失败', icon: 'none' });
      }
    },
    clearCache() {
      uni.showModal({
        title: '清除缓存',
        content: '将清除本地页面缓存与草稿，但保留登录态，是否继续？',
        success: (res) => {
          if (!res.confirm) return;
          const userInfo = uni.getStorageSync('userInfo');
          const token = uni.getStorageSync('medpal_token');
          uni.clearStorageSync();
          if (userInfo?.id) {
            uni.setStorageSync('userInfo', userInfo);
          }
          if (token) {
            uni.setStorageSync('medpal_token', token);
          }
          uni.showToast({ title: '缓存已清除', icon: 'success' });
          setTimeout(() => {
            uni.navigateBack({
              fail: () => {
                uni.switchTab({ url: '/pages/profile/index' });
              }
            });
          }, 600);
        }
      });
    }
  }
};
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: #f4f6fb;
}

.card {
  background: #ffffff;
  border-radius: 18rpx;
  padding: 18rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14rpx;
}

.label {
  font-size: 24rpx;
  color: #1c2b4a;
}

.desc {
  display: block;
  font-size: 20rpx;
  color: #9aa4b8;
  margin-top: 6rpx;
}

.password-form {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.btn-primary-inline {
  background: #2f65f9;
  color: #ffffff;
  border-radius: 999rpx;
  padding: 14rpx 0;
  font-size: 24rpx;
}

.btn-ghost {
  margin-top: 16rpx;
  background: #ffffff;
  border: 1rpx solid rgba(47, 101, 249, 0.3);
  color: #2f65f9;
  border-radius: 999rpx;
  padding: 14rpx 0;
}
</style>
