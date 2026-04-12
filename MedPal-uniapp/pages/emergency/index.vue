<template>
  <view class="page">
    <view class="header">
      <view class="icon"></view>
      <text class="title">紧急求助</text>
      <text class="subtitle">遇到紧急情况请立即填写并提交</text>
    </view>

    <view class="card">
      <view class="section-title">当前位置</view>
      <view class="location" @tap="getLocation">
        <text>{{ locationName || '获取位置中...' }}</text>
        <uni-icons type="right" size="16" color="#9aa4b8"></uni-icons>
      </view>
      <text v-if="locationError" class="error">{{ locationError }}</text>
    </view>

    <view class="card">
      <view class="section-title">问题描述</view>
      <uni-easyinput
        type="textarea"
        v-model="content"
        placeholder="请详细描述紧急情况和需要的帮助"
        maxlength="500"
      />
    </view>

    <view class="card">
      <view class="section-title">快捷选择</view>
      <view class="tag-list">
        <view
          v-for="item in quickTags"
          :key="item"
          class="tag"
          :class="{ active: selectedTag === item }"
          @tap="selectTag(item)"
        >
          {{ item }}
        </view>
      </view>
    </view>

    <view class="card tips">
      <view class="section-title">温馨提示</view>
      <view class="tip">1. 请保持手机畅通，工作人员会尽快联系您</view>
      <view class="tip">2. 如情况危急，请直接拨打急救电话 120</view>
      <view class="tip">3. 尽量描述清楚症状和具体位置</view>
    </view>

    <button class="btn-danger" :disabled="!content" @tap="submit">立即求助</button>
    <text class="hint">点击后将通知附近的工作人员</text>
  </view>
</template>

<script>
import { emergencyApi } from '@/utils/api.js';

export default {
  data() {
    return {
      content: '',
      locationName: '',
      locationError: '',
      latitude: null,
      longitude: null,
      selectedTag: '',
      quickTags: ['身体不适', '突发疾病', '跌倒受伤', '药物反应', '心理紧急', '其他紧急']
    };
  },
  onLoad() {
    this.getLocation();
  },
  methods: {
    getLocation() {
      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          this.latitude = Number(res.latitude);
          this.longitude = Number(res.longitude);
          this.locationName = `${res.latitude.toFixed(4)}, ${res.longitude.toFixed(4)}`;
          this.locationError = '';
        },
        fail: () => {
          this.latitude = null;
          this.longitude = null;
          this.locationError = '请授权位置权限以便快速定位';
        }
      });
    },
    selectTag(tag) {
      this.selectedTag = this.selectedTag === tag ? '' : tag;
      if (this.selectedTag && !this.content.includes(tag)) {
        this.content = this.content ? `${this.content}，${tag}` : tag;
      }
    },
    async submit() {
      if (!this.content.trim()) {
        return uni.showToast({ title: '请描述问题', icon: 'none' });
      }
      const user = uni.getStorageSync('userInfo');
      if (!user?.id) {
        uni.showToast({ title: '请先登录', icon: 'none' });
        return uni.navigateTo({ url: '/pages/login/index' });
      }
      try {
        await emergencyApi.create({
          userId: user.id,
          content: this.content.trim(),
          location: this.locationName || '',
          latitude: this.latitude,
          longitude: this.longitude,
          status: 'pending'
        });
        uni.showModal({
          title: '求助已提交',
          content: '工作人员会尽快联系您，如情况紧急请直接拨打 120。',
          showCancel: false,
          success: () => {
            this.content = '';
            this.selectedTag = '';
            this.latitude = null;
            this.longitude = null;
            uni.switchTab({ url: '/pages/index/index' });
          }
        });
      } catch (error) {
        uni.showToast({ title: error.message || '提交失败', icon: 'none' });
      }
    }
  }
};
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: #fef5f5;
}

.header {
  background: #ffeded;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 18rpx;
}

.icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 16rpx;
  background: #ff5c5c;
  margin-bottom: 12rpx;
}

.title {
  font-size: 28rpx;
  font-weight: 700;
  color: #c83b3b;
}

.subtitle {
  display: block;
  font-size: 22rpx;
  color: #a87474;
  margin-top: 6rpx;
}

.card {
  background: #ffffff;
  border-radius: 18rpx;
  padding: 18rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 10rpx 24rpx rgba(200, 59, 59, 0.08);
}

.section-title {
  font-size: 24rpx;
  font-weight: 600;
  color: #c83b3b;
  margin-bottom: 12rpx;
}

.location {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 22rpx;
  color: #5f6f94;
}

.error {
  color: #ff5c5c;
  font-size: 20rpx;
  margin-top: 8rpx;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.tag {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: #fff5f5;
  color: #c83b3b;
  font-size: 22rpx;
}

.tag.active {
  background: #ff5c5c;
  color: #ffffff;
}

.tips .tip {
  font-size: 22rpx;
  color: #8b5c5c;
  margin-bottom: 8rpx;
}

.btn-danger {
  background: #ff5c5c;
  color: #ffffff;
  border-radius: 999rpx;
  padding: 16rpx 0;
  font-size: 24rpx;
  margin-top: 6rpx;
}

.hint {
  display: block;
  text-align: center;
  font-size: 20rpx;
  color: #a87474;
  margin-top: 10rpx;
}
</style>
