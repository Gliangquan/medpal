<template>
  <view class="page">
    <view class="hero-card">
      <text class="hero-title">重置密码</text>
      <text class="hero-desc">为确保账户安全，请输入当前密码并设置新的登录密码。</text>
    </view>

    <view class="card">
      <view class="password-form">
        <uni-easyinput v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" />
        <uni-easyinput v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码（至少6位）" />
        <uni-easyinput v-model="passwordForm.checkPassword" type="password" placeholder="请再次输入新密码" />
        <button class="btn-primary-inline" @tap="submitPasswordChange">确认重置密码</button>
      </view>
    </view>
  </view>
</template>

<script>
import { userApi } from '@/utils/api.js';

export default {
  data() {
    return {
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        checkPassword: ''
      }
    };
  },
  methods: {
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

.hero-card {
  background: linear-gradient(145deg, #2f65f9, #5a84ff);
  border-radius: 24rpx;
  padding: 28rpx 24rpx;
  color: #ffffff;
  box-shadow: 0 14rpx 32rpx rgba(47, 101, 249, 0.18);
}

.hero-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
}

.hero-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.92);
}

.card {
  margin-top: 20rpx;
  background: #ffffff;
  border-radius: 18rpx;
  padding: 22rpx 18rpx;
  box-shadow: 0 10rpx 24rpx rgba(33, 56, 100, 0.06);
}

.password-form {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.btn-primary-inline {
  margin-top: 8rpx;
  background: #2f65f9;
  color: #ffffff;
  border-radius: 999rpx;
  padding: 16rpx 0;
  font-size: 24rpx;
}
</style>
