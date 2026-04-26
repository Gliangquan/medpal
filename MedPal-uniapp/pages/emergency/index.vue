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
        <text class="location-text">{{ locationDisplayText }}</text>
      </view>

      <!-- #ifdef H5 -->
      <view class="map-panel">
        <view id="emergency-amap" class="amap-container"></view>
        <view v-if="mapInitializing" class="map-mask">地图加载中...</view>
        <view v-else-if="mapUnavailable" class="map-mask error-mask">地图加载失败，请稍后重试</view>
      </view>
      <text class="map-hint">已接入高德地图，可点击地图修正求助位置</text>
      <!-- #endif -->

      <!-- #ifndef H5 -->
      <map
        class="mini-map"
        :latitude="mapLatitude"
        :longitude="mapLongitude"
        :scale="mapScale"
        :markers="mapMarkers"
        :show-location="true"
      />
      <text class="map-hint">可先自动定位，再使用地图选点修正具体位置</text>
      <!-- #endif -->

      <view class="action-row">
        <button class="btn-location" :disabled="locating" @tap="getLocation">
          {{ locating ? '定位中...' : (hasLocation ? '重新定位' : '获取当前位置') }}
        </button>
        <!-- #ifdef MP-WEIXIN -->
        <button class="btn-location btn-outline" @tap="chooseLocationOnMap">地图选点</button>
        <!-- #endif -->
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
      <view class="tip">3. 如自动定位不准，请手动修正地图位置</view>
    </view>

    <button class="btn-danger" :disabled="!content.trim()" @tap="submit">立即求助</button>
    <text class="hint">点击后将通知可处理的陪诊员，并在上方显示响应进度</text>
  </view>
</template>

<script>
import { emergencyApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';
import { DEFAULT_CENTER, formatCoords, loadAmapSdk } from '@/utils/amap.js';

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
      quickTags: ['身体不适', '突发疾病', '跌倒受伤', '药物反应', '心理紧急', '其他紧急'],
      mapLatitude: DEFAULT_CENTER.latitude,
      mapLongitude: DEFAULT_CENTER.longitude,
      mapScale: 16,
      mapMarkers: [],
      mapInitializing: false,
      mapUnavailable: false,
      h5Map: null,
      h5Marker: null,
      h5Geocoder: null
    };
  },
  computed: {
    hasLocation() {
      return typeof this.latitude === 'number' && typeof this.longitude === 'number';
    },
    locationDisplayText() {
      if (this.locationName) return this.locationName;
      if (this.locating) return '定位中...';
      return '暂未获取位置';
    }
  },
  onLoad() {
    this.getLocation();
    this.loadLatestHelp();
  },
  onReady() {
    this.initializeMap();
  },
  onShow() {
    this.loadLatestHelp();
  },
  onUnload() {
    this.destroyMap();
  },
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
    async initializeMap() {
      // #ifdef H5
      if (this.h5Map || this.mapInitializing) return;
      this.mapInitializing = true;
      this.mapUnavailable = false;
      try {
        const AMap = await loadAmapSdk();
        if (!AMap) {
          throw new Error('高德地图未就绪');
        }
        this.h5Map = new AMap.Map('emergency-amap', {
          zoom: this.mapScale,
          center: [this.mapLongitude, this.mapLatitude],
          viewMode: '2D'
        });
        this.h5Marker = new AMap.Marker({
          position: [this.mapLongitude, this.mapLatitude],
          anchor: 'bottom-center'
        });
        this.h5Map.add(this.h5Marker);
        this.h5Map.addControl(new AMap.Scale());
        this.h5Map.addControl(new AMap.ToolBar({ position: 'RB' }));
        this.h5Geocoder = new AMap.Geocoder({ radius: 1000, extensions: 'base' });
        this.h5Map.on('click', async (event) => {
          const longitude = Number(event.lnglat.getLng().toFixed(6));
          const latitude = Number(event.lnglat.getLat().toFixed(6));
          await this.applyLocation({ latitude, longitude, source: 'manual' });
          uni.showToast({ title: '已更新地图位置', icon: 'none' });
        });
        if (this.hasLocation) {
          this.syncMap(this.latitude, this.longitude, this.locationName);
        }
      } catch (error) {
        console.error('AMap init failed', error);
        this.mapUnavailable = true;
        this.locationError = this.locationError || '高德地图加载失败';
      } finally {
        this.mapInitializing = false;
      }
      // #endif
    },
    destroyMap() {
      // #ifdef H5
      if (this.h5Map && typeof this.h5Map.destroy === 'function') {
        this.h5Map.destroy();
      }
      this.h5Map = null;
      this.h5Marker = null;
      this.h5Geocoder = null;
      // #endif
    },
    async getLocation() {
      if (this.locating) return;
      this.locating = true;
      this.locationError = '';
      try {
        const result = await this.requestCurrentLocation();
        await this.applyLocation({
          latitude: result.latitude,
          longitude: result.longitude,
          source: 'device'
        });
      } catch (error) {
        await this.handleLocateFail(error);
      } finally {
        this.locating = false;
      }
    },
    requestCurrentLocation() {
      // #ifdef H5
      if (typeof navigator !== 'undefined' && navigator.geolocation) {
        return new Promise((resolve, reject) => {
          navigator.geolocation.getCurrentPosition(
            (position) => resolve(position.coords || {}),
            reject,
            {
              enableHighAccuracy: true,
              timeout: 10000,
              maximumAge: 0
            }
          );
        });
      }
      // #endif
      return new Promise((resolve, reject) => {
        uni.getLocation({
          type: 'gcj02',
          isHighAccuracy: true,
          highAccuracyExpireTime: 3000,
          success: resolve,
          fail: reject
        });
      });
    },
    async handleLocateFail(error) {
      const message = this.toLocationFailMessage(error);
      try {
        const fallback = await this.fallbackLocateByIp();
        await this.applyLocation({
          latitude: fallback.latitude,
          longitude: fallback.longitude,
          locationName: fallback.locationName,
          source: 'network'
        });
        this.locationError = message;
      } catch (fallbackError) {
        this.latitude = null;
        this.longitude = null;
        this.locationName = '';
        this.locationError = message;
      }
    },
    toLocationFailMessage(error) {
      const code = error?.code;
      const errMsg = error?.errMsg || '';
      if (code === 1 || errMsg.includes('auth deny') || errMsg.includes('permission')) {
        return '未获得定位权限，已尝试网络定位';
      }
      if (code === 2) {
        return '无法获取精确位置，已尝试网络定位';
      }
      if (code === 3 || errMsg.includes('timeout')) {
        return '定位超时，已尝试网络定位';
      }
      return '定位失败，已尝试网络定位';
    },
    async fallbackLocateByIp() {
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
      const parts = [result.city, result.region, result.country].filter(Boolean);
      return {
        latitude: Number(result.latitude.toFixed(6)),
        longitude: Number(result.longitude.toFixed(6)),
        locationName: parts.length
          ? `${parts.join(' · ')}（网络定位）`
          : `${formatCoords(result.latitude, result.longitude)}（网络定位）`
      };
    },
    async applyLocation({ latitude, longitude, locationName = '', source = 'device' }) {
      const lat = Number(Number(latitude).toFixed(6));
      const lng = Number(Number(longitude).toFixed(6));
      if (Number.isNaN(lat) || Number.isNaN(lng)) {
        throw new Error('invalid location');
      }
      this.latitude = lat;
      this.longitude = lng;
      let resolvedName = locationName;
      if (!resolvedName) {
        resolvedName = await this.resolveLocationName(lat, lng);
      }
      this.locationName = resolvedName || formatCoords(lat, lng);
      if (source !== 'network') {
        this.locationError = '';
      }
      this.syncMap(lat, lng, this.locationName);
    },
    async resolveLocationName(latitude, longitude) {
      // #ifdef H5
      const address = await this.reverseGeocodeByH5Map(latitude, longitude);
      if (address) {
        return address;
      }
      // #endif
      return `当前位置 ${formatCoords(latitude, longitude)}`;
    },
    async reverseGeocodeByH5Map(latitude, longitude) {
      // #ifdef H5
      try {
        if (!this.h5Geocoder) {
          await this.initializeMap();
        }
        if (!this.h5Geocoder) {
          return '';
        }
        const result = await new Promise((resolve, reject) => {
          this.h5Geocoder.getAddress([longitude, latitude], (status, data) => {
            if (status === 'complete' && data?.regeocode) {
              resolve(data.regeocode.formattedAddress || '');
              return;
            }
            reject(new Error('reverse geocode failed'));
          });
        });
        return result || '';
      } catch (error) {
        return '';
      }
      // #endif
      return '';
    },
    syncMap(latitude, longitude, label = '') {
      this.mapLatitude = latitude;
      this.mapLongitude = longitude;
      this.mapMarkers = [
        {
          id: 1,
          latitude,
          longitude,
          width: 28,
          height: 36,
          callout: {
            content: this.buildMarkerText(label),
            display: 'ALWAYS',
            padding: 6,
            borderRadius: 8,
            bgColor: '#ffffff',
            color: '#333333'
          }
        }
      ];
      // #ifdef H5
      if (this.h5Marker) {
        this.h5Marker.setPosition([longitude, latitude]);
      }
      if (this.h5Map) {
        this.h5Map.setZoomAndCenter(this.mapScale, [longitude, latitude]);
      }
      // #endif
    },
    buildMarkerText(label) {
      const text = label || '紧急求助位置';
      return text.length > 20 ? `${text.slice(0, 20)}...` : text;
    },
    chooseLocationOnMap() {
      // #ifdef MP-WEIXIN
      uni.chooseLocation({
        latitude: this.mapLatitude,
        longitude: this.mapLongitude,
        success: async (res) => {
          const locationName = [res.name, res.address].filter(Boolean).join(' · ') || formatCoords(res.latitude, res.longitude);
          await this.applyLocation({
            latitude: res.latitude,
            longitude: res.longitude,
            locationName,
            source: 'manual'
          });
        },
        fail: (error) => {
          if (error?.errMsg?.includes('cancel')) {
            return;
          }
          this.locationError = '地图选点失败，请稍后重试';
        }
      });
      // #endif
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

.map-panel,
.mini-map {
  width: 100%;
  height: 360rpx;
  margin-top: 16rpx;
  border-radius: 18rpx;
  overflow: hidden;
  background: #f7f8fc;
}

.map-panel {
  position: relative;
}

.amap-container {
  width: 100%;
  height: 100%;
}

.map-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  color: #8b5c5c;
  background: rgba(255, 255, 255, 0.72);
}

.error-mask {
  color: #ff5c5c;
}

.map-hint {
  display: block;
  margin-top: 10rpx;
  font-size: 20rpx;
  color: #8b5c5c;
}

.action-row {
  display: flex;
  gap: 16rpx;
  margin-top: 12rpx;
}

.btn-location {
  flex: 1;
  border-radius: 999rpx;
  background: rgba(255, 92, 92, 0.1);
  color: #c83b3b;
  border: 1rpx solid rgba(255, 92, 92, 0.18);
  font-size: 22rpx;
}

.btn-outline {
  background: #ffffff;
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
