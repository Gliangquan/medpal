<template>
  <view class="page">
    <view class="header">
      <view class="icon"></view>
      <text class="title">紧急求助</text>
      <text class="subtitle">遇到紧急情况请立即填写并提交</text>
    </view>

    <view v-if="latestHelp" class="card latest-card">
      <view class="section-title">最近一次求助进度</view>
      <view class="progress-row">
        <text class="progress-status" :class="latestHelp.status">{{ latestHelp.statusText }}</text>
        <text class="progress-time">{{ latestHelp.createTimeText }}</text>
      </view>
      <text class="progress-content">{{ latestHelp.content }}</text>
      <text v-if="latestHelp.responderText" class="progress-helper">{{ latestHelp.responderText }}</text>
      <text v-if="latestHelp.resolveNote" class="progress-helper">处理说明：{{ latestHelp.resolveNote }}</text>
    </view>

    <view class="card">
      <view class="section-title">当前位置</view>
      <view class="location-box">
        <text class="location-text">{{ locationName || (locating ? '定位中...' : '暂未获取位置') }}</text>
      </view>
      <button class="btn-location" :disabled="locating" @tap="getLocation">{{ locating ? '定位中...' : '获取当前位置' }}</button>
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
    <text class="hint">点击后将通知可处理的陪诊员，并在上方显示响应进度</text>
  </view>
</template>

<script>
import { emergencyApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';

const STATUS_TEXT = {
  pending: '已提交，等待响应',
  responded: '已有陪诊员响应',
  resolved: '本次求助已处理'
};

export default {
  data() {
    return {
      content: '',
      locationName: '',
      locationError: '',
      locating: false,
      latitude: null,
      longitude: null,
      selectedTag: '',
      latestHelp: null,
      quickTags: ['身体不适', '突发疾病', '跌倒受伤', '药物反应', '心理紧急', '其他紧急']
    };
  },
  onLoad() {
    this.getLocation();
    this.loadLatestHelp();
  },
  onShow() {
    this.loadLatestHelp();
  },
  computed: {},
  methods: {
    async loadLatestHelp() {
      const user = uni.getStorageSync('userInfo');
      if (!user?.id) return;
      try {
        const page = await emergencyApi.list({ current: 1, size: 5, userId: user.id });
        const list = page?.records || [];
        if (!list.length) {
          this.latestHelp = null;
          return;
        }
        const latest = list[0];
        this.latestHelp = {
          ...latest,
          statusText: STATUS_TEXT[latest.status] || '处理中',
          createTimeText: formatDateTime(latest.createTime) || '刚刚',
          responderText: latest.responderId ? `已有陪诊员接收（陪诊员 #${latest.responderId}）` : ''
        };
      } catch (error) {
        this.latestHelp = null;
      }
    },
    async getLocation() {
      if (this.locating) return;
      this.locating = true;
      this.locationError = '';
      const handleFail = async (message) => {
        try {
          await this.fallbackLocateByIp(message);
        } catch (error) {
          this.latitude = null;
          this.longitude = null;
          this.locationName = '';
          this.locationError = message || '定位失败，请检查定位权限';
          this.locating = false;
        }
      };

      if (typeof navigator !== 'undefined' && navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords || {};
            if (typeof latitude !== 'number' || typeof longitude !== 'number') {
              handleFail('定位失败，请稍后重试');
              return;
            }
            this.latitude = Number(latitude.toFixed(6));
            this.longitude = Number(longitude.toFixed(6));
            this.locationName = `${this.latitude.toFixed(4)}, ${this.longitude.toFixed(4)}`;
            this.locating = false;
          },
          (error) => {
            const code = error?.code;
            if (code === 1) {
              handleFail('浏览器定位不可用，已切换为网络定位');
              return;
            }
            if (code === 2) {
              handleFail('无法获取精确位置，已切换为网络定位');
              return;
            }
            handleFail('定位超时，已切换为网络定位');
          },
          {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 0
          }
        );
        return;
      }

      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          this.latitude = Number(res.latitude);
          this.longitude = Number(res.longitude);
          this.locationName = `${res.latitude.toFixed(4)}, ${res.longitude.toFixed(4)}`;
          this.locating = false;
        },
        fail: async (error) => {
          console.error('getLocation failed', error);
          await handleFail('请授权位置权限以便快速定位');
        }
      });
    },
    async fallbackLocateByIp(message) {
      const result = await new Promise((resolve, reject) => {
        uni.request({
          url: 'https://ipwho.is/',
          method: 'GET',
          success: (res) => resolve(res.data),
          fail: reject
        });
      });
      if (!result || result.success === false || typeof result.latitude !== 'number' || typeof result.longitude !== 'number') {
        throw new Error('fallback failed');
      }
      this.latitude = Number(result.latitude.toFixed(6));
      this.longitude = Number(result.longitude.toFixed(6));
      const parts = [result.city, result.region, result.country].filter(Boolean);
      this.locationName = parts.length ? `${parts.join(' · ')}（网络定位）` : `${this.latitude.toFixed(4)}, ${this.longitude.toFixed(4)}（网络定位）`;
      this.locationError = message || '已切换为网络定位';
      this.locating = false;
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
          content: this.content.trim(),
          location: this.locationName || '',
          latitude: this.latitude,
          longitude: this.longitude,
          status: 'pending'
        });
        uni.showModal({
          title: '求助已提交',
          content: '系统已通知可处理的陪诊员，你可返回本页查看最新响应进度；如情况紧急请直接拨打 120。',
          showCancel: false,
          success: async () => {
            this.content = '';
            this.selectedTag = '';
            this.latitude = null;
            this.longitude = null;
            await this.loadLatestHelp();
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

.latest-card {
  border: 1rpx solid rgba(255, 92, 92, 0.15);
  background: #fffafa;
}

.progress-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-status {
  font-size: 24rpx;
  font-weight: 700;
}

.progress-status.pending {
  color: #ff8a00;
}

.progress-status.responded {
  color: #2f65f9;
}

.progress-status.resolved {
  color: #6f7a8d;
}

.progress-time,
.progress-helper {
  display: block;
  margin-top: 8rpx;
  font-size: 21rpx;
  color: #8b5c5c;
}

.progress-content {
  display: block;
  margin-top: 10rpx;
  font-size: 23rpx;
  color: #4f5564;
  line-height: 1.6;
}

.section-title {
  font-size: 24rpx;
  font-weight: 600;
  color: #c83b3b;
  margin-bottom: 12rpx;
}

.location-box {
  min-height: 120rpx;
  border-radius: 18rpx;
  background: #fff5f5;
  border: 1rpx dashed rgba(255, 92, 92, 0.18);
  display: flex;
  align-items: center;
  padding: 0 24rpx;
}

.location-text {
  font-size: 22rpx;
  color: #5f6f94;
  line-height: 1.6;
}

.btn-location {
  width: 100%;
  margin-top: 12rpx;
  border-radius: 999rpx;
  background: rgba(255, 92, 92, 0.1);
  color: #c83b3b;
  border: 1rpx solid rgba(255, 92, 92, 0.18);
  font-size: 22rpx;
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
